package kabranov.games.bubbleburstgame;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import kabranov.games.bubbleburstgame.R;
import kabranov.games.bubbleburstgame.ScoreText;


public class BubbleActivity extends Activity {

	// These variables are for testing purposes, do not modify
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final String TAG = "BubbleBurstGame";

	// The Main view
	private RelativeLayout mFrame;
	
	// Score view
	private ScoreText mScoreText;

	// Bubble image's bitmap
	private Bitmap mBitmap;

	// Display dimensions
	private int mDisplayWidth, mDisplayHeight;

	// Sound variables

	// AudioManager
	private AudioManager mAudioManager;
	// SoundPool
	private SoundPool mSoundPool;
	// ID for the bubble popping sound
	private int mSoundID;
	// Audio volume
	private float mStreamVolume;

	// Gesture Detector
	private GestureDetector mGestureDetector;
	
	// Number of bubbles
	//private int nBubbles=0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Set up user interface
		mFrame = (RelativeLayout) findViewById(R.id.frame);
	    mScoreText= (ScoreText)findViewById(R.id.scoreText);
		
		// Load basic bubble Bitmap
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Manage bubble popping sound
		// Use AudioManager.STREAM_MUSIC as stream type

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mStreamVolume = (float) mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// TODO - make a new SoundPool, allowing up to 10 streams
		mSoundPool = 	mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);;

		// TODO - set a SoundPool OnLoadCompletedListener that calls
		// setupGestureDetector()
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				setupGestureDetector();
			}
		});
		
		// TODO - load the sound from res/raw/bubble_pop.wav
		mSoundID = mSoundPool.load(this, R.raw.bubble_pop, 1);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			// Get the size of the display so this View knows where borders are
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();
		}
	}

	// Set up GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,
		new GestureDetector.SimpleOnGestureListener() {

			// If a fling gesture starts on a BubbleView then change the
			// BubbleView's velocity

			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {

				// TODO - Implement onFling actions.
				// You can get all Views in mFrame one at a time
				// using the ViewGroup.getChildAt() method
				
				Log.i(TAG, "Fling: velocity: "+velocityX+"  "+velocityY);
				Log.i(TAG, "Fling: event   : "+event1.getRawX()+"  "+event1.getRawY());
				Log.i(TAG, "Fling: views count = "+ mFrame.getChildCount());
				
					for(int i=0;i<mFrame.getChildCount();i++){
						BubbleView view=(BubbleView) mFrame.getChildAt(i);
						 Log.i(TAG, "Fling: view coords: "+(view.mXPos+view.mRadius)+"  "+(view.mXPos+view.mRadius)+"  radius:"+view.mRadius);
						 if (view.intersects(event1.getRawX(),(event1.getRawY()-(getWindowManager().getDefaultDisplay().getHeight()-mDisplayHeight)))){
							 view.deflect(velocityX,velocityY);
							 return true;
					}
				}		
				return false;
			}

			// If a single tap intersects a BubbleView, then pop the BubbleView
			// Otherwise, create a new BubbleView at the tap's location and add
			// it to mFrame. You can get all views from mFrame with
			// ViewGroup.getChildAt()

			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {

				// TODO - Implement onSingleTapConfirmed actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method

					int pointerIndex = event.getActionIndex();
					int pointerID = event.getPointerId(pointerIndex);

					//float x=event.getX(pointerIndex);
					//float y=event.getY(pointerIndex);
					float x=event.getRawX();
					float y=event.getRawY();
				
					
					// if the tap is outside exsisting buble, than we create new one
					boolean newBubble=true;
					
					for(int i=0;i<mFrame.getChildCount();i++){
						Log.i(TAG, "mFrame("+i+") type: "+mFrame.getChildAt(i).getClass().getSimpleName());
						if(mFrame.getChildAt(i) instanceof ScoreText  ) return true;
						
						BubbleView view=(BubbleView) mFrame.getChildAt(i);
						if (view.intersects(event.getRawX(),event.getRawY())){
							newBubble=false;  // we found a bubble
							view.stopMovement(true);
							//mFrame.removeViewAt(i);
							ScoreText.nPopped++;
							mScoreText.invalidate();
							break;
						}
					}
					
					if(newBubble){
						//Log.i(TAG, "Bubble: coord   : "+event.getX(pointerIndex)+"  "+event.getY(pointerIndex));
						//BubbleView view=new BubbleView(getApplicationContext(),event.getX(pointerIndex),event.getY(pointerIndex));
						Log.i(TAG, "Bubble: coord   : " +event.getRawX()+ "  "+event.getRawY());
						Log.i(TAG, "Bubble: coord xy: " +event.getX()+ "  "+event.getY());
						BubbleView view=new BubbleView(getApplicationContext(),event.getRawX(),event.getRawY());
						
						mFrame.addView(view);
						view.startMovement();						
					}
					
				return true;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// TODO - Delegate the touch to the gestureDetector
		return mGestureDetector.onTouchEvent(event);
		
		//return true || false;		
	}

	@Override
	protected void onPause() {
		// TODO - Release all SoundPool resources
		if (null != mSoundPool) {
			mSoundPool.unload(mSoundID);
			mSoundPool.release();
			mSoundPool = null;
		}

		//mAudioManager.setSpeakerphoneOn(false);
		//mAudioManager.unloadSoundEffects();
		
		super.onPause();
	}

	// BubbleView is a View that displays a bubble.
	// This class handles animating, drawing, and popping amongst other actions.
	// A new BubbleView is created for each bubble on the display

	public class BubbleView extends View {

		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mScaledBitmapWidth;
		private Bitmap mScaledBitmap;
		
		private int nCurrentBubble;

		// location, speed and direction of the bubble
		private float mXPos, mYPos, mDx, mDy, mRadius, mRadiusSquared;
		private long mRotate, mDRotate;

		BubbleView(Context context, float x, float y) {
			super(context);

			// Create a new random number generator to
			// randomize size, rotation, speed and direction
			Random r = new Random();

			// Creates the bubble bitmap for this BubbleView
			createScaledBitmap(r);

			// Radius of the Bitmap
			mRadius = (float) mScaledBitmapWidth / (float)2.0;
			mRadiusSquared = mRadius * mRadius;
			
			// Adjust position to center the bubble under user's finger
			//mXPos = x - mRadius;
			//mYPos = y - mRadius;
			
			mXPos = x;
			mYPos = y-(getWindowManager().getDefaultDisplay().getHeight()-mDisplayHeight);
			
			// Set the BubbleView's speed and direction
			setSpeedAndDirection(r);

			// Set the BubbleView's rotation
			setRotation(r);

			mPainter.setAntiAlias(true);
			mPainter.setColor(Color.BLUE); 
			mPainter.setTypeface(Typeface.SANS_SERIF);
			mPainter.setTextSize(ScoreText.letterSize);
			Log.i(TAG, "Bubble: created   : " +mXPos+ "  "+mYPos+"  rad="+mRadius);
		}

		private void setRotation(Random r) {
			if (speedMode == RANDOM) {
				// TODO - set rotation in range [1..3]
				mDRotate=r.nextInt(3)+1;
			} else {
				mDRotate = 0;
			}
		}

		private void setSpeedAndDirection(Random r) {
			// Used by test cases
			switch (speedMode) {
			case SINGLE:
				mDx = 20;
				mDy = 20;
				break;

			case STILL:
				// No speed
				mDx = 0;
				mDy = 0;
				break;

			default:
				// TODO - Set mDx and mDy to indicate movement direction and speed 
				// Limit speed in the x and y direction to [-3..3] pixels per movement.
				
		        mDx=r.nextInt(7)-3;
		        mDy=r.nextInt(7)-3;	
			}
		}

		private void createScaledBitmap(Random r) {

			if (speedMode != RANDOM) {
				mScaledBitmapWidth = BITMAP_SIZE * 3;
			} else {
				// TODO - set scaled bitmap size in range [1..3] * BITMAP_SIZE
				mScaledBitmapWidth=(r.nextInt(3)+1)* BITMAP_SIZE;		
			}

			// TODO - create the scaled bitmap using size set above
			this.mScaledBitmap = Bitmap.createScaledBitmap(mBitmap,
					mScaledBitmapWidth, mScaledBitmapWidth, false);

		}

		// Start moving the BubbleView & updating the display
		private void startMovement() {
			
			// increase number of bubbles
			nCurrentBubble=++ScoreText.nBubbles;
			mScoreText.invalidate();
			// Creates a WorkerThread
			ScheduledExecutorService executor = Executors
					.newScheduledThreadPool(1);

			// Execute the run() in Worker Thread every REFRESH_RATE
			// milliseconds
			// Save reference to this job in mMoverFuture
			mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {

					// TODO - implement movement logic.
					// Each time this method is run the BubbleView should
					// move one step. If the BubbleView exits the display,
					// stop the BubbleView's Worker Thread.
					// Otherwise, request that the BubbleView be redrawn.

					removeColided();
					
					if (moveWhileOnScreen() ) {				
						stopMovement(true);					
					} else {
						postInvalidate();
					}		
				}
			}, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
		}

		// Returns true if the BubbleView intersects position (x,y)
		private synchronized boolean intersects(float x, float y) {

			// TODO - Return true if the BubbleView intersects position (x,y)
			 if(  ((x-mXPos)*(x-mXPos) + (y-mYPos)*(y-mYPos)) < 4*(mRadius*mRadius) ){			
				 return true;
			}			
			return  false;
		}

		
		private  boolean collidesWith(BubbleView bubble){
			
			float localX=mXPos ; 
			float localY=mYPos ;
			
			float bubbleX=bubble.mXPos; 
			float bubbleY=bubble.mYPos;
			
			Log.i(TAG, "localX="+localX+" localY="+localY);
			Log.i(TAG, "bubbleX="+bubbleX+" bubbleY="+bubbleY);
			Log.i(TAG, "this radius="+this.mRadius+" bubble radius="+bubble.mRadius);
			
			if ( (localX-bubbleX)*(localX-bubbleX)+(localY-bubbleY)*(localY-bubbleY)<=(this.mRadius+bubble.mRadius)*(this.mRadius+bubble.mRadius)){
				Log.i(TAG, "returns true");
				return true;

			}
			Log.i(TAG, "returns false");
			return false;
			
		}
		
		
		private  void removeColided(){
			for(int i=0;i<mFrame.getChildCount();i++){
				//Log.i(TAG, "COLLISION mFrame("+i+") type: "+mFrame.getChildAt(i).getClass().getSimpleName());
				if(mFrame.getChildAt(i) instanceof ScoreText  ) continue;
				if(mFrame.getChildAt(i) == this  ) continue;
				
				
				BubbleView view=(BubbleView) mFrame.getChildAt(i);
				//if (view.collidesWith(this)){
				if (collidesWith(view)){
					//Log.i(TAG, "COLIDED WITH  type: "+this.getClass().getSimpleName());
					view.stopMovement(true);
					//mFrame.removeViewAt(i);
					
					stopMovement(true);
					mScoreText.invalidate();
					break;
				}
				
				//mScoreText.invalidate();
				
			}
		}
		
		
		// Cancel the Bubble's movement
		// Remove Bubble from mFrame
		// Play pop sound if the BubbleView was popped

		private void stopMovement(final boolean wasPopped) {

			if (null != mMoverFuture) {

				if (!mMoverFuture.isDone()) {
					mMoverFuture.cancel(true);
				}

				// This work will be performed on the UI Thread
				mFrame.post(new Runnable() {
					@Override
					public void run() {

						// TODO - Remove the BubbleView from mFrame
						mFrame.removeView(BubbleView.this);

						
						// TODO - If the bubble was popped by user,
						// play the popping sound
						if (wasPopped) {							
								mSoundPool.play(mSoundID, mStreamVolume,mStreamVolume, 1, 0, 1.0f);														
						}
					}
				});
			}
		}

		// Change the Bubble's speed and direction
		private synchronized void deflect(float velocityX, float velocityY) {
			mDx = velocityX / REFRESH_RATE;
			mDy = velocityY / REFRESH_RATE;
		}

		// Draw the Bubble at its current location
		@Override
		protected synchronized void onDraw(Canvas canvas) {

			// TODO - save the canvas
			canvas.save();

			// TODO - increase the rotation of the original image by mDRotate
			mRotate+=mDRotate;
			
			// TODO Rotate the canvas by current rotation
			// Hint - Rotate around the bubble's center, not its position
			canvas.rotate(mRotate, mXPos , mYPos );
			
			// TODO - draw the bitmap at it's new location
			 canvas.drawBitmap(mScaledBitmap, mXPos-mRadius, mYPos-mRadius, mPainter);

			 canvas.drawText(nCurrentBubble+"", mXPos - mRadius + mScaledBitmapWidth/2,  mYPos-mRadius + mScaledBitmapWidth/2, mPainter);
			
			// TODO - restore the canvas
			 canvas.restore();            		
		}

		// Returns true if the BubbleView is still on the screen after the move
		// operation
		private synchronized boolean moveWhileOnScreen() {

			// TODO - Move the BubbleView			
				 mXPos = mXPos+mDx;
				 mYPos = mYPos+mDy;
						
			return isOutOfView();
		}

		// Return true if the BubbleView is still on the screen after the move
		// operation
		private boolean isOutOfView() {

			// TODO - Return true if the BubbleView is still on the screen after
			// the move operation
			if ((mXPos+mScaledBitmapWidth/2) < 0 || mXPos + mScaledBitmapWidth/2 > mDisplayWidth || (mYPos + mScaledBitmapWidth/2)< 0 || mYPos + mScaledBitmapWidth/2 > mDisplayHeight){
				return true;
			} else {
				return false;
			}
			//return true || false;
		}
	}

	// Do not modify below here

	@Override
	public void onBackPressed() {
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	/*	case R.id.menu_still_mode:
			speedMode = STILL;
			return true;
		case R.id.menu_single_speed:
			speedMode = SINGLE;
			return true;
		case R.id.menu_random_mode:
			speedMode = RANDOM;
			return true;*/
		case R.id.about:
			Intent activityIntent= new Intent(getApplicationContext(),AboutActivity.class);
			startActivity(activityIntent);
			return true;
		case R.id.font_size_up:
			ScoreText.letterSize = ScoreText.letterSize+5;
			mScoreText.invalidate();
			return true;
		case R.id.font_size_down:
			ScoreText.letterSize = ScoreText.letterSize-5;
			mScoreText.invalidate();
			return true;		
		case R.id.reset:
			ScoreText.nBubbles=0;
			ScoreText.nPopped=0;
			ScoreText.nMissed=0;
			//mScoreText.invalidate();
						
			for(int i=0;i<mFrame.getChildCount();i++){
				Log.i(TAG, "mFrame("+i+") type: "+mFrame.getChildAt(i).getClass().getSimpleName());
				if(mFrame.getChildAt(i) instanceof ScoreText  ) return true;				
				BubbleView view=(BubbleView) mFrame.getChildAt(i);
				view.stopMovement(true);
			}
			return true;
		case R.id.quit:
			exitRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void exitRequested() {
		super.onBackPressed();
	}
}