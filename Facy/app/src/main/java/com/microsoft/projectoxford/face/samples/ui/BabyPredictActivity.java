package com.microsoft.projectoxford.face.samples.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.samples.R;
import com.microsoft.projectoxford.face.samples.helper.ImageHelper;
import com.microsoft.projectoxford.face.samples.helper.LogHelper;
import com.microsoft.projectoxford.face.samples.helper.SampleApp;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BabyPredictActivity extends AppCompatActivity {

    // Background task of Baby Predict.
    private class BabyPredict extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        true,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[]{
                                FaceServiceClient.FaceAttributeType.Age,
                                FaceServiceClient.FaceAttributeType.Gender,
                                FaceServiceClient.FaceAttributeType.Smile,
                                FaceServiceClient.FaceAttributeType.Glasses,
                                FaceServiceClient.FaceAttributeType.FacialHair,
                                FaceServiceClient.FaceAttributeType.Emotion,
                                FaceServiceClient.FaceAttributeType.HeadPose,
                                FaceServiceClient.FaceAttributeType.Accessories,
                                FaceServiceClient.FaceAttributeType.Blur,
                                FaceServiceClient.FaceAttributeType.Exposure,
                                FaceServiceClient.FaceAttributeType.Hair,
                                FaceServiceClient.FaceAttributeType.Makeup,
                                FaceServiceClient.FaceAttributeType.Noise,
                                FaceServiceClient.FaceAttributeType.Occlusion
                        });


            } catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
            addLog("Request: Detecting in image " + mImageUri1);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            if (mSucceed) {
                addLog
                        ("Response: Success. Detected " + (result == null ? 0 : result.length)
                                + " face(s) in " + mImageUri1);
            }

            // Show the result on screen when detection is done.
            if (check == 0) {
                setUiAfterDetection(result, mSucceed, 0);
            } else if (check == 1) {
                setUiAfterDetection(result, mSucceed, 1);
            }
        }
    }

    // check number image
    private int check = 0;

    //Gender of baby wanted
    private String genderOfBaby = "";

    //Skin of baby wanted
    private String skinOfBaby = "";

    ImageView babyImage;


    // Check two image input are detected face
    private boolean foundFace1 = false;
    private boolean foundFace2 = false;

    // Check gender must difference
    private int genderFace1 = 0;
    private int genderFace2 = 0;

    // Url Image API
    private String urlImage = "";

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE_1 = 0;

    private static final int REQUEST_SELECT_IMAGE_2 = 1;

    // The URI of the image selected to detect.
    private Uri mImageUri1;
    private Uri mImageUri2;

    // The image selected to detect.
    private Bitmap mBitmap1;
    private Bitmap mBitmap2;

    // Progress dialog popped up when communicating with server.
    ProgressDialog mProgressDialog;


    // When the activity is created, set all the member variables to initial state.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_predict);
        babyImage = (ImageView) findViewById(R.id.babyImage);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));

        // Disable button "share" and "show baby" as the image to detect is not selected.
        setShowAndShareButtonStatus(false);

        LogHelper.clearDetectionLog();
    }

    // Save the activity state when it's going to stop.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("ImageUri1", mImageUri1);
        outState.putParcelable("ImageUri2", mImageUri2);
    }

    // Recover the saved state when the activity is recreated.
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mImageUri1 = savedInstanceState.getParcelable("ImageUri1");
        if (mImageUri1 != null) {
            mBitmap1 = ImageHelper.loadSizeLimitedBitmapFromUri(
                    mImageUri1, getContentResolver());
        }

        mImageUri2 = savedInstanceState.getParcelable("ImageUri2");
        if (mImageUri2 != null) {
            mBitmap2 = ImageHelper.loadSizeLimitedBitmapFromUri(
                    mImageUri2, getContentResolver());
        }
    }


    // Called when the "Select Image" button is clicked.
    public void loadImage1(View view) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_1);
    }

    public void loadImage2(View view) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_2);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE_1:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri1 = null;
                    mBitmap1 = null;
                    mImageUri1 = data.getData();
                    mBitmap1 = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri1, getContentResolver());
                    if (mBitmap1 != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.image_0);
                        imageView.setImageBitmap(mBitmap1);

                        // Add detection log.
                        addLog("Image: " + mImageUri1 + " resized to " + mBitmap1.getWidth()
                                + "x" + mBitmap1.getHeight());
                    }
                    // Clear the information panel.
                    setInfo("");

                    // Enable button "show baby" as the image to detect is not selected.
                    setShowButtonsEnabledStatus(true);
                }
                break;
            case REQUEST_SELECT_IMAGE_2:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri2 = null;
                    mBitmap2 = null;
                    mImageUri2 = data.getData();
                    mBitmap2 = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri2, getContentResolver());
                    if (mBitmap2 != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.image_1);
                        imageView.setImageBitmap(mBitmap2);

                        // Add detection log.
                        addLog("Image: " + mImageUri2 + " resized to " + mBitmap2.getWidth()
                                + "x" + mBitmap2.getHeight());
                    }
                    // Clear the information panel.
                    setInfo("");

                    // Enable button "show baby" as the image to detect is not selected.
                    setShowButtonsEnabledStatus(true);
                }
                break;
            default:
                break;
        }
    }

    // Called when the "showBaby" button is clicked.
    public void showBaby(View view) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        mBitmap1.compress(Bitmap.CompressFormat.JPEG, 100, output1);
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(output1.toByteArray());

        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        mBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, output2);
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(output2.toByteArray());

        // Start a background task to detect faces in the image.
        new BabyPredictActivity.BabyPredict().execute(inputStream1);
        new BabyPredictActivity.BabyPredict().execute(inputStream2);


        // Prevent button click during detecting.
        setAllButtonsEnabledStatus(false);
    }

    // Called when the "share" button is clicked.
    public void share(View view) {
        if(urlImage != "") {
            try {
                Intent mIntentFacebook = new Intent();
                mIntentFacebook.setClassName("com.facebook.katana",
                        "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias");
                mIntentFacebook.setAction("android.intent.action.SEND");
                mIntentFacebook.setType("text/plain");
                mIntentFacebook.putExtra("android.intent.extra.TEXT", urlImage);
                startActivity(mIntentFacebook);
            } catch (Exception e) {
                e.printStackTrace();
                Intent mIntentFacebookBrowser = new Intent(Intent.ACTION_SEND);
                String mStringURL = "https://www.facebook.com/sharer/sharer.php?u=" + urlImage;
                mIntentFacebookBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(mStringURL));
                startActivity(mIntentFacebookBrowser);
            }
        }
    }


    // Add a log item.
    private void addLog(String log) {
        LogHelper.addDetectionLog(log);
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    // Set whether the buttons are enabled.
    private void setShowAndShareButtonStatus(boolean isEnabled) {
        Button showBaby = (Button) findViewById(R.id.showBaby);
        Button share = (Button) findViewById(R.id.share);

        showBaby.setEnabled(isEnabled);
        share.setEnabled(isEnabled);
    }

    // Set whether the show button is enabled.
    private void setShowButtonsEnabledStatus(boolean isEnabled) {
        Button showBaby = (Button) findViewById(R.id.showBaby);
        showBaby.setEnabled(isEnabled);
    }

    // Set whether the share button is enabled.
    private void setShareButtonsEnabledStatus(boolean isEnabled) {
        Button share = (Button) findViewById(R.id.share);
        share.setEnabled(isEnabled);
    }

    // Set whether the buttons are enabled.
    private void setAllButtonsEnabledStatus(boolean isEnabled) {
        Button showBaby = (Button) findViewById(R.id.showBaby);
        Button share = (Button) findViewById(R.id.share);
        Button loadImg1 = (Button) findViewById(R.id.loadImg1);
        Button loadImg2 = (Button) findViewById(R.id.loadImg2);

        showBaby.setEnabled(isEnabled);
        share.setEnabled(isEnabled);
        loadImg1.setEnabled(isEnabled);
        loadImg2.setEnabled(isEnabled);
    }

    // Show the result on screen when detection is done.
    private void setUiAfterDetection(Face[] result, boolean succeed, int checkImage) {

        // Detection is done, hide the progress dialog.
        mProgressDialog.dismiss();

        // Enable all the buttons.
        setAllButtonsEnabledStatus(true);

        // Disable button "detect" as the image has already been detected.
        setShareButtonsEnabledStatus(false);
        setShowButtonsEnabledStatus(false);
        if (checkImage == 0) {
            if (succeed) {
                check = 1;
                // The information about the detection result.
                if (result.length != 0) {
                    foundFace1 = true;
                    // Show the detected faces on original image.
                    ImageView imageView = (ImageView) findViewById(R.id.image_0);
                    imageView.setImageBitmap(ImageHelper.drawFaceRectanglesOnBitmap(
                            mBitmap1, result, true));
                    genderFace1 = result[0].faceAttributes.gender.startsWith("male") ? 0 : 1;

                }
            }
        } else {
            if (succeed) {
                // The information about the detection result.
                check = 0;
                if (result.length != 0) {
                    foundFace2 = true;
                    // Show the detected faces on original image.
                    ImageView imageView = (ImageView) findViewById(R.id.image_1);
                    imageView.setImageBitmap(ImageHelper.drawFaceRectanglesOnBitmap(
                            mBitmap2, result, true));
                    genderFace2 = result[0].faceAttributes.gender.startsWith("male") ? 0 : 1;
                }
            }

            String detectionResult;
            if (foundFace1 && foundFace2 && (genderFace1 != genderFace2)) {
                setShareButtonsEnabledStatus(true);
                foundFace1 = foundFace2 = false;
                genderFace1 = genderFace2 = 0;
                getImageBaby();
                detectionResult = getString(R.string.found_baby);
                setInfo(detectionResult);
            } else if (foundFace1 && foundFace2 && (genderFace1 == genderFace2)) {
                setShareButtonsEnabledStatus(false);
                babyImage.setImageResource(0);
                foundFace1 = foundFace2 = false;
                genderFace1 = genderFace2 = 0;
                detectionResult = getString(R.string.same_gender);
                setInfo(detectionResult);
            } else {
                setShareButtonsEnabledStatus(false);
                babyImage.setImageResource(0);
                foundFace1 = foundFace2 = false;
                genderFace1 = genderFace2 = 0;
                detectionResult = getString(R.string.not_found_baby);
                setInfo(detectionResult);
            }
        }


    }

    private void getImageBaby() {
         urlImage = "https://babypredict.herokuapp.com/baby?" +
                "gender=" + genderOfBaby
                + "&skin=" + skinOfBaby;
        //Loading image using Picasso
        Picasso.get().load(urlImage).resize(50, 50).into(babyImage);
        Toast.makeText(this,urlImage,Toast.LENGTH_LONG).show();
    }

    public void onRadioButtonGenderClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton_male:
                if (checked)
                    genderOfBaby = "boy";
                break;
            case R.id.radioButton_female:
                if (checked)
                    genderOfBaby = "girl";
                break;
        }
    }

    public void onRadioButtonSkinClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton_light:
                if (checked)
                    skinOfBaby = "light";
                break;
            case R.id.radioButton_dark:
                if (checked)
                    skinOfBaby = "dark";
                break;
        }
    }
}
