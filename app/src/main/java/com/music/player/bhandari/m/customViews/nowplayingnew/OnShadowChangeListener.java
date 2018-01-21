package com.music.player.bhandari.m.customViews.nowplayingnew;

/**
 * Interface for shadow change listener.
 */
interface OnShadowChangeListener {

    /**
     * Set shadow percentages for diffusers.
     *
     * @param bigDiffuserShadowPercentage    shadow percentage for big diffuser (0.0f - 1.0f)
     * @param mediumDiffuserShadowPercentage shadow percentage for medium diffuser (0.0f - 1.0f)
     * @param smallDiffuserShadowPercentage  shadow percentage for small diffuser (0.0f - 1.0f)
     */
    void shadowChanged(float bigDiffuserShadowPercentage, float mediumDiffuserShadowPercentage, float smallDiffuserShadowPercentage);
}
