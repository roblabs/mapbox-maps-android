package com.mapbox.maps.plugin.locationcomponent.animators

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.os.Build
import android.view.animation.LinearInterpolator
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants
import com.mapbox.maps.plugin.locationcomponent.LocationLayerRenderer

internal abstract class PuckAnimator<T>(
  evaluator: TypeEvaluator<T>
) : ValueAnimator() {

  private val userConfiguredAnimator: ValueAnimator
  protected var locationRenderer: LocationLayerRenderer? = null

  abstract fun updateLayer(fraction: Float, value: T)

  init {
    setObjectValues(emptyArray<Any>())
    setEvaluator(
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
        Evaluators.OBJECT
      else
        evaluator
    )
    addUpdateListener {
      @Suppress("UNCHECKED_CAST")
      updateLayer(it.animatedFraction, it.animatedValue as T)
    }
    duration = LocationComponentConstants.DEFAULT_INTERVAL_MILLIS
    interpolator = DEFAULT_INTERPOLATOR
    userConfiguredAnimator = clone()
  }

  /**
   * Set the animator object values
   *
   * @param values varags of object values
   */
  final override fun setObjectValues(vararg values: Any?) {
    super.setObjectValues(*values)
  }

  /**
   * Set the animator evaluator
   *
   * @param value the animator type evaluator
   */
  final override fun setEvaluator(value: TypeEvaluator<*>?) {
    super.setEvaluator(value)
  }

  /**
   * Adds a listener to the set of listeners that are sent update events through the life of
   * an animation. This method is called on all listeners for every frame of the animation,
   * after the values for the animation have been calculated.
   *
   * @param listener the listener to be added to the current set of listeners for this animation.
   */
  final override fun addUpdateListener(listener: AnimatorUpdateListener?) {
    super.addUpdateListener(listener)
  }

  final override fun clone(): ValueAnimator {
    return super.clone()
  }

  fun setLocationLayerRenderer(renderer: LocationLayerRenderer) {
    locationRenderer = renderer
  }

  fun animate(vararg targets: T, options: (ValueAnimator.() -> Unit)? = null) {
    if (isRunning) {
      cancel()
    }
    if (userConfiguredAnimator.isRunning) {
      userConfiguredAnimator.cancel()
    }
    if (options == null) {
      setObjectValues(*targets)
      start()
    } else {
      options.invoke(userConfiguredAnimator)
      userConfiguredAnimator.setObjectValues(*targets)
      userConfiguredAnimator.start()
    }
  }

  fun updateOptions(block: ValueAnimator.() -> Unit) {
    if (isRunning) {
      addListener(object : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
          super.onAnimationEnd(animation, isReverse)
          block.invoke(this@PuckAnimator)
          removeListener(this)
        }
      })
    } else {
      block.invoke(this@PuckAnimator)
    }
  }

  fun cancelRunning() {
    if (isRunning) {
      cancel()
    }
    if (userConfiguredAnimator.isRunning) {
      userConfiguredAnimator.cancel()
    }
  }

  companion object {
    private val DEFAULT_INTERPOLATOR = LinearInterpolator()
  }
}