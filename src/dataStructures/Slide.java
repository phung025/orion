/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

/**
 *
 * @author Nam Phung
 */
public class Slide {

    private int slideSize = 0;
    private DataPoint[] container = null;
    private int insertionIndex = 0;
    private int count = 0;

    /**
     *
     * @param size
     */
    public Slide(int size) {
        this.slideSize = size;
        
        this.container = new DataPoint[this.slideSize];
        this.count = 0;
        for (int i = 0; i < this.container.length; ++i) {
            container[i] = null;
        }
    }

    /**
     * Add an incoming data point to the slide. If the slide is full, remove the
     * last data point in the slide and then add the new data point to the slide.
     * @param p
     * @return true if the data point was added successfully, else return false.
     */
    public boolean add(DataPoint p) {

        // Add data point to the slide, if full, wraps around
        this.container[insertionIndex] = p;
        insertionIndex = (insertionIndex + 1) % this.slideSize;

        // Increment the count
        this.count = (this.count == this.slideSize) ? this.slideSize : (this.count + 1);
        
        return true;
    }

    /**
     * Get the latest data point
     *
     * @return the head of this slide, or null if this slide is empty
     */
    public DataPoint newest() {
        return this.isEmpty() ? null : (this.isFull() ? this.container[(insertionIndex - 1) % this.slideSize] : this.container[insertionIndex - 1]);
    }
    
    public DataPoint oldest() {
        return this.isEmpty() ? null : (this.isFull() ? this.container[insertionIndex] : this.container[0]);
    }
    
    public int size() {
        return this.count;
    }

    /**
     * Check if the slide is full at the moment or not
     *
     * @return true if the slide is full else false
     */
    public boolean isFull() {
        return this.count == this.slideSize;
    }
    
    /**
     * Check if the slide is empty
     * @return true if the slide is empty else false
     */
    public boolean isEmpty() {
        return this.count == 0;
    }
    
    /**
     * Return all ordered data points in the slide
     * @return array of data points
     */
    public DataPoint[] points() {
        return this.container;
    }
    
    /**
     * Get the maximum capacity of the slide
     * @return maximum amount of data points the slide can hold
     */
    public int capacity() {
        return this.slideSize;
    }
}
