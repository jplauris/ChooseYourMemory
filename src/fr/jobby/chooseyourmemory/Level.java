package fr.jobby.chooseyourmemory;

public class Level {
    private int level;
    private int xNumber;
    private int yNumber;
    private int color;

    
    /**
     * @param name
     * @param xNumber
     * @param yNumber
     */
    public Level(int level, int xNumber, int yNumber, int color) {
        super();
        this.setLevel(level);
        this.setxNumber(xNumber);
        this.setyNumber(yNumber);
        this.setColor(color);
    }


    /**
     * @return the name
     */
    public int getLevel() {
        return level;
    }


    /**
     * @param name the name to set
     */
    public void setLevel(int level) {
        this.level = level;
    }


    /**
     * @return the xNumber
     */
    public int getxNumber() {
        return xNumber;
    }


    /**
     * @param xNumber the xNumber to set
     */
    public void setxNumber(int xNumber) {
        this.xNumber = xNumber;
    }


    /**
     * @return the yNumber
     */
    public int getyNumber() {
        return yNumber;
    }


    /**
     * @param yNumber the yNumber to set
     */
    public void setyNumber(int yNumber) {
        this.yNumber = yNumber;
    }


    /**
     * @return the color
     */
    public int getColor() {
        return color;
    }


    /**
     * @param color the color to set
     */
    public void setColor(int color) {
        this.color = color;
    }
    
}
