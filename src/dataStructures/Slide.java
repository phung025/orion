/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import java.util.LinkedList;

/**
 *
 * @author Nam Phung
 * @param <E>
 */
public class Slide<E> extends LinkedList<E> {

    private int slideSize = 0;

    /**
     *
     * @param size
     */
    public Slide(int size) {
        this.slideSize = size;
    }

    /**
     * Add an incoming data point to the slide. If the slide is full, remove the
     * last data point in the slide and then add the new data point to the slide.
     * @param p
     * @return true if the data point was added successfully, else return false.
     */
    @Override
    public boolean add(E p) {

        // If the slide is full, remove the oldest data point from the slide
        // then add the new incoming data point. Else, add the incoming data
        // point to the slide
        try {
            if (this.isFull()) {
                super.remove();
                super.add(p);
            } else {
                super.add(p);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Get the data point that has the arrival time closest to the current time
     * in the slide
     *
     * @return the head of this slide, or null if this slide is empty
     */
    @Override
    public E peek() {
        return (super.isEmpty()) ? null : super.getLast();
    }

    /**
     * Retrieves and removes the data point that has the arrival time closest to the
     * current time in the slide.
     *
     * @return the head of this slide, or null if this slide is empty
     */
    @Override
    public E poll() {
        return (super.isEmpty()) ? null : super.removeLast();
    }

    /**
     * Check if the slide is full at the moment or not
     *
     * @return true if the slide is full else false
     */
    public boolean isFull() {
        return super.size() == this.slideSize;
    }
}
