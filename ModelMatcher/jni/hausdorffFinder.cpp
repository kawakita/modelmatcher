#include <string.h>
#include <jni.h>
#pragma warning(disable : 4996) // Don't warn about using sprintf
#pragma warning(disable : 4530) // Don't warn about not enabling exceptions

#include <stdio.h>
#include <time.h>
#include <opencv2/legacy/legacy.hpp>
#include <android/log.h>

// Image processing stuff
#include "image.h"
#include "hausdorff.h"
#include "cv.h"
#include "highgui.h"
#include "shape.hpp"
#include <opencv2/core/core.hpp>
#include <opencv2/opencv_modules.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "opencv2/shape/shape_distance.hpp"
#include "opencv2/shape/shape_transformer.hpp"
#include "opencv2/shape/hist_cost.hpp"
//#include "opencv2/shape/src/hist_cost.cpp"
#include "opencv2/shape/src/haus_dis.cpp"
//#include "opencv2/shape/src/sc_dis.cpp"
//#include "opencv2/shape/test/test_precomp.hpp"

using namespace cv;

const char MATCH_PREVIEW_WINDOW_TITLE[] = "Match Preview";

// The image to search for in the haystack image
Image<Rgb>* needleImage;
Image<Intensity>* needleEdges;
Image<Intensity32F>* needleDistanceTransform;

// The image to search for the needle image in
Image<Rgb>* haystackImage;
Image<Intensity>* haystackEdges;
Image<Intensity32F>* haystackDistanceTransform;

// Used to display a preview of the needle edge image overlaid on the haystack
// edge image along with the computed Hausdorff distance between the two.
Image<Rgb>* matchPreviewImage;
CvFont* font;

/*
 * Finds the translation of needle in haystack that results in the minimal Hausdorff distance.
 */
CvPoint findBestTranslation(int step = 2, double* dist = 0,
                            int minX = 0, int minY = 0,
                            int maxX = -1, int maxY = -1)
{
    // Find the optimum translation
    unsigned bestX = 0;
    unsigned bestY = 0;
    double bestDistance = std::numeric_limits<double>::max();

    const int maxOffsetX = maxX != -1 ? maxX : haystackDistanceTransform->width() - needleEdges->width();
    const int maxOffsetY = maxY != -1 ? maxY : haystackDistanceTransform->height() - needleEdges->height();
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n max offset x %d", maxOffsetX);
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n max offset y %d", maxOffsetY);
    //needleEdges->save("/mnt/sdcard/ModelMatcher/milk/needle.jpg");
    //haystackEdges->save("/mnt/sdcard/ModelMatcher/milk/haystack.jpg");
    for (int y = minY; y < maxOffsetY; y += step)
    {
        for (int x = minX; x < maxOffsetX; x += step)
        {
            const double forwardDist = findHausdorffDistance(*needleEdges, *haystackDistanceTransform, cvPoint(x, y));
            const double reverseDist = findHausdorffDistance(*haystackEdges, *needleDistanceTransform, cvPoint(-x, -y));
            //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n forward distance %f", forwardDist);
            //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n reverse distance %f", reverseDist);
            const double dist = std::max<double>(forwardDist, reverseDist);

            if (dist < bestDistance)
            {
                bestDistance = dist;
                bestX = x;
                bestY = y;
            }
        }
    }

    if (dist)
        *dist = bestDistance;

    return cvPoint(bestX, bestY);
}

/*
 * Finds the translation of needle in haystack that results in the minimal Hausdorff distance
 * by recurively calling findBestTranslation() for successively finer step sizes as we get
 * closer and closer to a solution.
 */
CvPoint findBestTranslationRecursive(int initialStep = 32, double* dist = 0)
{
    double bestDistance = std::numeric_limits<double>::max();
    CvPoint bestTranslation;

    int minX = 0;
    int minY = 0;
    const int absoluteMaxX = haystackDistanceTransform->width() - needleEdges->width();
    const int absoluteMaxY = haystackDistanceTransform->height() - needleEdges->height();
    int maxX = absoluteMaxX;
    int maxY = absoluteMaxY;

    for (int step = initialStep; step > 0; step /= 2)
    {
        double distance;
        CvPoint translation = findBestTranslation(
                                  step, &distance,
                                  minX, minY,
                                  maxX, maxY);
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n current distance %f", distance);
        if (distance < bestDistance)
        {
            bestDistance = distance;
            bestTranslation = translation;

            minX = std::max<int>(0, translation.x - step);
            minY = std::max<int>(0, translation.y - step);
            maxX = std::min<int>(absoluteMaxX, translation.x + step);
            maxY = std::min<int>(absoluteMaxY, translation.y + step);
        }
    }

    if (dist)
        *dist = bestDistance;

    return bestTranslation;
}

/*
 * Finds the translation of needle in haystack that results in the minimal Hausdorff distance,
 * allowing for some variation in scale and rotation of the needle in the haystack image.
 */
vector <Point> convertContourType(const Mat& currentQuery, int n)
{
    vector<vector<Point> > _contoursQuery;
    vector <Point> contoursQuery;
    findContours(currentQuery, _contoursQuery, RETR_LIST, CHAIN_APPROX_NONE);
    for (size_t border=0; border<_contoursQuery.size(); border++)
    {
        for (size_t p=0; p<_contoursQuery[border].size(); p++)
        {
            contoursQuery.push_back(_contoursQuery[border][p]);
        }
    }

    // In case actual number of points is less than n
    for (int add=(int)contoursQuery.size()-1; add<n; add++)
    {
        contoursQuery.push_back(contoursQuery[contoursQuery.size()-add+1]); //adding dummy values
    }

    // Uniformly sampling
    random_shuffle(contoursQuery.begin(), contoursQuery.end());
    int nStart=n;
    vector<Point> cont;
    for (int i=0; i<nStart; i++)
    {
        cont.push_back(contoursQuery[i]);
    }
    return cont;
}
vector<Point2f> normalizeContour(const vector<Point> &contour)
{
    vector<Point2f> output(contour.size());
    Mat disMat((int)contour.size(),(int)contour.size(),CV_32F);
    Point2f meanpt(0,0);
    float meanVal=1;

    for (int ii=0, end1 = (int)contour.size(); ii<end1; ii++)
    {
        for (int jj=0, end2 = (int)contour.size(); end2; jj++)
        {
            if (ii==jj) disMat.at<float>(ii,jj)=0;
            else
            {
                disMat.at<float>(ii,jj)=
                    float(fabs(double(contour[ii].x*contour[jj].x)))+float(fabs(double(contour[ii].y*contour[jj].y)));
            }
        }
        meanpt.x+=contour[ii].x;
        meanpt.y+=contour[ii].y;
    }
    meanpt.x/=contour.size();
    meanpt.y/=contour.size();
    meanVal=float(cv::mean(disMat)[0]);
    for (size_t ii=0; ii<contour.size(); ii++)
    {
        output[ii].x = (contour[ii].x-meanpt.x)/meanVal;
        output[ii].y = (contour[ii].y-meanpt.y)/meanVal;
    }
    return output;
}
float computeShapeDistance(vector <Point>& query1, vector <Point>& query2,
                                         vector <Point>& query3, vector <Point>& testq)
{
    Ptr <HausdorffDistanceExtractor> haus = createHausdorffDistanceExtractor();
    return std::min(haus->computeDistance(query1,testq), std::min(haus->computeDistance(query2,testq),
                             haus->computeDistance(query3,testq)));
}
double findBestTranslationScaleAndRotation(
    CvPoint* bestTranslation,
    int* bestRotation,
    double* bestScale,
    int initialTranslationStep = 32,
    int minRotation = -32,
    int maxRotation = 32,
    int rotationStep = 4,
    double minScale = 0.5,
    double maxScale = 2.0,
    double scaleStep = 0.25)
{
    double bestDistance = std::numeric_limits<double>::max();

    CvPoint2D32f center = cvPoint2D32f(needleEdges->width() / 2, needleEdges->height() / 2);
    CvMat* rotMat = cvCreateMat(2, 3, CV_32FC1);
    CvMat* bestRotMat = cvCreateMat(2, 3, CV_32FC1);
    Image<Intensity> backupPrior(needleEdges->width(), needleEdges->height());
    cvCopy(*needleEdges, backupPrior);

    double heightneedle = needleEdges->height();
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n height of needle %f", heightneedle);

    for (int rotation = minRotation; rotation <= maxRotation; rotation += rotationStep)
    {
        double angle = rotation * 3.14 / 180.0;
        for (double scale = minScale; scale <= maxScale; scale += scaleStep)
        {
            cv2DRotationMatrix(center, angle, scale, rotMat);
            cvWarpAffine(backupPrior, *needleEdges, rotMat, 1+8, cvScalarAll(255));

            double dist;
            CvPoint point = findBestTranslationRecursive(initialTranslationStep, &dist);
            __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n distance %f", dist);

            if (dist < bestDistance)
            {
                bestDistance = dist;
                if (bestTranslation)
                    *bestTranslation = point;
                if (bestRotation)
                    *bestRotation = rotation;
                if (bestScale)
                    *bestScale = scale;
                cvCopy(rotMat, bestRotMat);
                //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n best rotation %f, best scale %f", bestRotation, bestScale);

            }
        }
    }
    cvWarpAffine(backupPrior, *needleEdges, bestRotMat, 1+8, cvScalarAll(255));
    return bestDistance;
}

/*
 * Draws the translated needle shape in the haystack image.
 */
void drawTranslatedPrior(const CvPoint& point, double dist)
{
    // Superimpose the translated needle image on the haystack image
    cvCopy(*haystackImage, *matchPreviewImage);
    cvSetImageROI(*matchPreviewImage, cvRect(point.x, point.y, needleImage->width(), needleImage->height()));
    cvCopy(*needleImage, *matchPreviewImage);
    cvResetImageROI(*matchPreviewImage);
    cvCircle(*matchPreviewImage,
             cvPoint(point.x + needleImage->width()/2 - 10, point.y + needleImage->height()/2 - 10),
             20, cvScalar(0, 0, 255), 3);

    // Print the haystackDistanceTransform in the top-left corner of the image
    char buffer[64];
    sprintf(buffer, "dist = %.2f", dist);
    cvRectangle(*matchPreviewImage, cvPoint(0, 0), cvPoint(200, 30), cvScalar(255,255,255), CV_FILLED);
    cvPutText(*matchPreviewImage, buffer, cvPoint(10,20), font, cvScalar(0,0,0));

    cvShowImage(MATCH_PREVIEW_WINDOW_TITLE, *matchPreviewImage);
}
/**
 * Function to check if the color of the given image
 * is the same as the given color
 *
 * Parameters:
 *   edge        The source image
 *   color   The color to check
 */
bool is_border(cv::Mat& edge, cv::Vec3b color)
{
    cv::Mat im = edge.clone().reshape(0,1);

    bool res = true;
    for (int i = 0; i < im.cols; ++i)
        res &= (color == im.at<cv::Vec3b>(0,i));

    return res;
}

/**
 * Function to auto-cropping image
 *
 * Parameters:
 *   src   The source image
 *   dst   The destination image
 */
void autocrop(cv::Mat& src, cv::Mat& dst)
{
    cv::Rect win(0, 0, src.cols, src.rows);

    std::vector<cv::Rect> edges;
    edges.push_back(cv::Rect(0, 0, src.cols, 1));
    edges.push_back(cv::Rect(src.cols-2, 0, 1, src.rows));
    edges.push_back(cv::Rect(0, src.rows-2, src.cols, 1));
    edges.push_back(cv::Rect(0, 0, 1, src.rows));

    cv::Mat edge;
    int nborder = 0;
    cv::Vec3b color = src.at<cv::Vec3b>(0,0);

    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n number of edges %d", edges.size());

    for (int i = 0; i < edges.size(); ++i)
    {
        edge = src(edges[i]);
        nborder += is_border(edge, color);
    }

    if (nborder < 4)
    {
        src.copyTo(dst);
        return;
    }

    bool next;

    do {
        edge = src(cv::Rect(win.x, win.height-2, win.width, 1));
        if (next = is_border(edge, color))
            win.height--;
    }
    while (next && win.height > 0);

    do {
        edge = src(cv::Rect(win.width-2, win.y, 1, win.height));
        if (next = is_border(edge, color))
            win.width--;
    }
    while (next && win.width > 0);

    do {
        edge = src(cv::Rect(win.x, win.y, win.width, 1));
        if (next = is_border(edge, color))
            win.y++, win.height--;
    }
    while (next && win.y <= src.rows);

    do {
        edge = src(cv::Rect(win.x, win.y, 1, win.height));
        if (next = is_border(edge, color))
            win.x++, win.width--;
    }
    while (next && win.x <= src.cols);

    dst = src(win);
}

extern "C" {

JNIEXPORT jint JNICALL
Java_com_CornellTech_ModelMatcher_HausdorffImageFinder_mainautocrop
(JNIEnv *env, jobject obj, jstring path1, jstring path2)
{
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	const char *filename1 = env->GetStringUTFChars(path1, 0);
	const char *filename2 = env->GetStringUTFChars(path2, 0);
    cv::Mat src = cv::imread(filename1);
    if (!src.data)
        return -1;

    cv::Mat dst, dst_gray, threshold_output, miniMat, roiImg;
    //autocrop(src, dst);
    cvtColor( src, dst, CV_BGR2GRAY );
    blur( dst, dst_gray, Size(3,3) );
    threshold( dst_gray, threshold_output, 100, 255, THRESH_BINARY );
    findContours( threshold_output, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

    /// Approximate contours to polygons + get bounding rects and circles
    vector<vector<Point> > contours_poly( contours.size() );
    vector<Rect> boundRect( contours.size() );
    vector<Point2f>center( contours.size() );
    vector<float>radius( contours.size() );
    Rect roi;

    float max_size = 0.0, center_max = 0.0;
    for( int i = 0; i < contours.size(); i++ )
       { approxPolyDP( Mat(contours[i]), contours_poly[i], 3, true );
         //boundRect[i] = boundingRect( Mat(contours_poly[i]) );
         boundRect[i] = boundingRect( contours[i]);
         minEnclosingCircle( (Mat)contours_poly[i], center[i], radius[i] );
         if(contours[i].size() > max_size)
         {
        	 max_size = contours[i].size();
        	 roi = boundRect[i];
         }
       }
	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n max radius %f", max_size);
	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n n contours %d", contours.size());
    miniMat = src(roi);
    miniMat.copyTo(roiImg);
    imwrite(filename2, roiImg);

    return 0;
}
JNIEXPORT jdouble JNICALL
Java_com_CornellTech_ModelMatcher_HausdorffImageFinder_hausdorff
/*(JNIEnv *env, jobject obj, jstring path1, jstring path2)*/
(JNIEnv *env, jobject obj, jlong addrNeedle, jlong addrHaystack)
{
	const Mat* pMatNeedle=(Mat*)addrNeedle;
	const Mat* pMatHaystack=(Mat*)addrHaystack;
	Mat bwNeedle, bwHaystack;
	cvtColor(*pMatNeedle, bwNeedle, CV_RGB2GRAY);
	cvtColor(*pMatHaystack, bwHaystack, CV_RGB2GRAY);
	vector<Point> needleContours, haystackContours;
	needleContours = convertContourType(bwNeedle, 180);
	haystackContours = convertContourType(bwHaystack, 180);
	const int angularBins=12;
	const int radialBins=4;
	const float minRad=0.2f;
	const float maxRad=2;
/*
	Ptr <ShapeContextDistanceExtractor> mysc = createShapeContextDistanceExtractor(angularBins, radialBins, minRad, maxRad);
    Ptr <HistogramCostExtractor> cost = createChiHistogramCostExtractor(30,0.15f);
    mysc->setIterations(1);
    mysc->setCostExtractor( cost );
    mysc->setTransformAlgorithm( createThinPlateSplineShapeTransformer() );
    return mysc->computeDistance(needleContours, haystackContours);
*/

    Ptr <HausdorffDistanceExtractor> haus = createHausdorffDistanceExtractor();
    //imwrite("/mnt/sdcard/ModelMatcher/lauren/needleContours.jpg",needleContours);
    //imwrite("/mnt/sdcard/ModelMatcher/lauren/haystackContours.jpg",haystackContours);
    return haus->computeDistance(needleContours,haystackContours);

}
}

