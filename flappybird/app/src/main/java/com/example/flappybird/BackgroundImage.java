package com.example.flappybird;

public class BackgroundImage {
    private int backgroundImageX;
    private int backgroundImageY;
    private final int getBackgroundVelocity;
    public BackgroundImage(){
        backgroundImageX= 0;
        backgroundImageY = 0;
        getBackgroundVelocity = 3;
    }
    public int getX(){
        return backgroundImageX ;
    }
    public int getY(){
        return backgroundImageY;
    }

    public void setX (int backgroundImageX){
        this.backgroundImageX = backgroundImageX;
    }

    public void setY (int backgroundImageY){
        this.backgroundImageY = backgroundImageY;
    }

    public int getVelocity(){
        return getBackgroundVelocity;
    }

}
