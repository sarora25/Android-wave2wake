package com.example.firstalarm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

public class UndoBarController {
	private static View mBarView;
	private static TextView mMessageView;
	private static ViewPropertyAnimator mBarAnimator;
	private static Handler mHideHandler = new Handler();

	private UndoListener mUndoListener;

	// State objects
	private static Parcelable mUndoToken;
	private static CharSequence mUndoMessage;

	public interface UndoListener {
		void onUndo(Parcelable token);
	}

	public UndoBarController(View undoBarView, UndoListener undoListener) {
		mBarView = undoBarView;
		mBarAnimator = mBarView.animate();
		mUndoListener = undoListener;

		mMessageView = (TextView) mBarView.findViewById(R.id.undobar_message);
		mBarView.findViewById(R.id.undobar_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						hideUndoBar(false);
						mUndoListener.onUndo(mUndoToken);
					}
				});

		hideUndoBar(true);
	}

	public void showUndoBar(boolean immediate, CharSequence message,
			Parcelable undoToken) {
		mUndoToken = undoToken;
		mUndoMessage = message;
		mMessageView.setText(mUndoMessage);

		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, mBarView.getResources()
				.getInteger(R.integer.undobar_hide_delay));

		mBarView.setVisibility(View.VISIBLE);
		if (immediate) {
			mBarView.setAlpha(1);
		} else {
			mBarAnimator.cancel();
			mBarAnimator
					.alpha(1)
					.setDuration(
							mBarView.getResources().getInteger(
									android.R.integer.config_shortAnimTime))
					.setListener(null);
		}
	}

	public static void hideUndoBar(boolean immediate) {
		mHideHandler.removeCallbacks(mHideRunnable);
		if (immediate) {
			mBarView.setVisibility(View.GONE);
			mBarView.setAlpha(0);
			mUndoMessage = null;
			mUndoToken = null;

		} else {
			mBarAnimator.cancel();
			mBarAnimator
					.alpha(0)
					.setDuration(
							mBarView.getResources().getInteger(
									android.R.integer.config_shortAnimTime))
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mBarView.setVisibility(View.GONE);
							mUndoMessage = null;
							mUndoToken = null;
						}
					});
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence("undo_message", mUndoMessage);
		outState.putParcelable("undo_token", mUndoToken);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mUndoMessage = savedInstanceState.getCharSequence("undo_message");
			mUndoToken = savedInstanceState.getParcelable("undo_token");

			if (mUndoToken != null || !TextUtils.isEmpty(mUndoMessage)) {
				showUndoBar(true, mUndoMessage, mUndoToken);
			}
		}
	}

	private static Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hideUndoBar(false);
		}
	};
}
