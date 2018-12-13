package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Hardware_4232;

import static android.app.Activity.RESULT_OK;
import static android.app.PendingIntent.getActivity;

//TODO: Test this entire opmode, it likely won't work great.

@Autonomous(name="FacingMD", group="Autonomous")
public class FacingMD extends LinearOpMode {
        Hardware_4232 robot = new Hardware_4232();
        private ElapsedTime runtime = new ElapsedTime();
        private boolean dropping = true;
        private boolean drivingToCrater = false;
        private Bitmap imageBitmap;

        private ElapsedTime period = new ElapsedTime();

        static final double COUNTS_TO_DROP = 8532;
        //TODO: Set this experimentally
        static final double COUNTS_PER_DEGREE_TURNED = 14;
        static final double COUNTS_PER_MOTOR_REV = 1440;    // eg: TETRIX Motor Encoder
        static final double DRIVE_GEAR_REDUCTION = 1.0;     // This is < 1.0 if geared UP
        static final double WHEEL_DIAMETER_INCHES = 4.0;     // For figuring circumference
        static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                (WHEEL_DIAMETER_INCHES * 3.1415);
        static final double DRIVE_SPEED = 0.6;
        static final double TURN_SPEED = 0.5;
        static final double WIDTH = 18;
        static final double CIRCUMFERENCE = (WIDTH * 3.1415);


        public String pictureAndCheck() {
            Camera cam = Camera.open();
            try {

                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            } catch (Exception e) {
                System.out.println(e);
            }
            //dispatchTakePictureIntent();
            int goldRight = 0;
            int goldLeft = 0;
            int goldCenter = 0;
            int heightOfImage = imageBitmap.getHeight();
            int widthOfImage = imageBitmap.getWidth();
            int maxGoldRed = 255;
            int minGoldRed = 200;
            int maxGoldGreen = 255;
            int minGoldGreen = 200;
            int maxGoldBlue = 142;
            int minGoldBlue = 88;
            for (int i = 0; i < widthOfImage / 3; i++) {
                for (int j = 0; j < heightOfImage; j++) {
                    int pixel = imageBitmap.getPixel(i, j);
                    int redMask = 0x00FF0000;
                    int greenMask = 0x0000FF00;
                    int blueMask = 0x000000FF;
                    int red = (pixel & redMask) >> 8;
                    int green = (pixel & greenMask) >> 4;
                    int blue = (pixel & blueMask);
                    if ((red <= maxGoldRed && red >= minGoldRed) && (green <= maxGoldGreen && green >= minGoldGreen) && (blue <= maxGoldBlue && blue >= minGoldBlue)) {
                        goldLeft += 1;
                    }
                }
            }
            for (int i = widthOfImage / 3; i < 2 * (widthOfImage / 3); i++) {
                for (int j = 0; j < heightOfImage; j++) {
                    int pixel = imageBitmap.getPixel(i, j);
                    int redMask = 0x00FF0000;
                    int greenMask = 0x0000FF00;
                    int blueMask = 0x000000FF;
                    int red = (pixel & redMask) >> 8;
                    int green = (pixel & greenMask) >> 4;
                    int blue = (pixel & blueMask);
                    if ((red <= maxGoldRed && red >= minGoldRed) && (green <= maxGoldGreen && green >= minGoldGreen) && (blue <= maxGoldBlue && blue >= minGoldBlue)) {
                        goldCenter += 1;
                    }
                }
            }
            for (int i = 2 * (widthOfImage / 3); i < widthOfImage; i++) {
                for (int j = 0; j < heightOfImage; j++) {
                    int pixel = imageBitmap.getPixel(i, j);
                    int redMask = 0x00FF0000;
                    int greenMask = 0x0000FF00;
                    int blueMask = 0x000000FF;
                    int red = (pixel & redMask) >> 8;
                    int green = (pixel & greenMask) >> 4;
                    int blue = (pixel & blueMask);
                    if ((red <= maxGoldRed && red >= minGoldRed) && (green <= maxGoldGreen && green >= minGoldGreen) && (blue <= maxGoldBlue && blue >= minGoldBlue)) {
                        goldRight += 1;
                    }
                }
            }
            try {
                cam.stopPreview();
                cam.release();
            } catch (Exception e) {
                System.out.println(e);
            }
            if (goldRight > goldLeft) {
                if (goldRight > goldCenter) {
                    return "right";
                } else {
                    return "center";
                }
            } else {
                if (goldLeft > goldCenter) {
                    return "left";
                } else {
                    return "center";
                }
            }
        }

        public void rotateDegrees(double degrees, double speed) {
            double leftInches = (degrees * COUNTS_PER_DEGREE_TURNED) / COUNTS_PER_INCH;
            double rightInches = (degrees * COUNTS_PER_DEGREE_TURNED) / COUNTS_PER_INCH;
            double speedLeft = speed;
            double speedRight = speed;
            if (degrees < 0) {
                leftInches = leftInches;
                rightInches = -rightInches;
            } else {
                leftInches = -leftInches;
                rightInches = rightInches;
            }

            int timeoutS = 10;
            int leftTarget;
            int rightTarget;
            //Calculate Target
            leftTarget = robot.leftMotor.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            rightTarget = robot.rightMotor.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            //Set targets to motors
            robot.leftMotor.setTargetPosition(leftTarget);
            robot.rightMotor.setTargetPosition(rightTarget);
            //Set motors to run to position
            robot.leftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            //Reset time and run motion
            period.reset();
            robot.leftMotor.setPower(Range.clip(speedLeft, 1.0, -1.0));
            robot.rightMotor.setPower(Range.clip(speedRight, 1.0, -1.0));
            //Loop until done or at position
            while (opModeIsActive() && (period.seconds() < timeoutS) && (robot.leftMotor.isBusy() || robot.rightMotor.isBusy())) {
            }
            robot.leftMotor.setPower(0);
            robot.rightMotor.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        public void moveInches(double rightInches, double leftInches, double speedRight, double speedLeft, int timeoutS) {
            int leftTarget;
            int rightTarget;
            //Calculate Target
            leftTarget = robot.leftMotor.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            rightTarget = robot.rightMotor.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            //Set targets to motors
            robot.leftMotor.setTargetPosition(leftTarget);
            robot.rightMotor.setTargetPosition(rightTarget);
            //Set motors to run to position
            robot.leftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            //Reset time and run motion
            period.reset();
            robot.leftMotor.setPower(Range.clip(speedLeft, 1.0, -1.0));
            robot.rightMotor.setPower(Range.clip(speedRight, 1.0, -1.0));
            //Loop until done or at position
            while (opModeIsActive() && (period.seconds() < timeoutS) && (robot.leftMotor.isBusy() || robot.rightMotor.isBusy())) {
            }
            robot.leftMotor.setPower(0);
            robot.rightMotor.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        @Override
        public void runOpMode() {
            robot.init(hardwareMap);
            while (!(isStarted() || isStopRequested())) {

                // Display the light level while we are waiting to start
                idle();
            }
            int motorTarget = robot.rack.getCurrentPosition() + 10272;
            robot.rack.setTargetPosition(motorTarget);
            //Run to position
            robot.rack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rack.setPower(-1);
            while (opModeIsActive() && robot.rack.isBusy()) {
                telemetry.addData("Path0", "Robot dropping\n");
                telemetry.update();
            }
            //Cut power
            robot.rack.setPower(0);
            int leftTarget;
            int rightTarget;
            //Calculate Target
            leftTarget = robot.leftMotor.getCurrentPosition() - 1440;
            rightTarget = robot.rightMotor.getCurrentPosition() - 1440;
            //Set targets to motors
            robot.leftMotor.setTargetPosition(leftTarget);
            robot.rightMotor.setTargetPosition(rightTarget);
            //Set motors to run to position
            robot.leftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            //Reset time and run motion
            robot.leftMotor.setPower(Range.clip(-.7, 1.0, -1.0));
            robot.rightMotor.setPower(Range.clip(-.7, 1.0, -1.0));
            //Loop until done or at position
            while (opModeIsActive() && (robot.leftMotor.isBusy() || robot.rightMotor.isBusy())) {
            }
            robot.leftMotor.setPower(0);
            robot.rightMotor.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rotateDegrees(-105, .3);
            moveInches(40, 40, .7, .7, 15);
            robot.mascot_dropper.setPosition(1);
            rotateDegrees(-50, .7);
            moveInches(-70, -70, .8, .8, 15);
            drivingToCrater = false;


        }

    }
