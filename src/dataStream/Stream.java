/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStream;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Nam Phung
 */
public class Stream {

    private long referenceTimeStamp = 0;
    private boolean timeBasedTimestamp = true;
    public final Queue<DataPoint> dataStream = new LinkedList();

    private Stream() {

    }

    /**
     * Parameterized constructor for the Stream object. The boolean variable
     * indicate the stream's timestamp is time-based or count-based. If true,
     * the timestamp for the stream will be set to be time-based. An incoming
     * data point will have a timestamp = arrival_time - created_stream_time.
     * The timestamp will be measured in milliseconds. If false, the timestamp
     * is set to be count-based. An incoming data point will have a timestamp
     * equals to the index of the data point in the stream.
     *
     * @param timeBasedTimestamp
     */
    public Stream(boolean timeBasedTimestamp) {
        if (timeBasedTimestamp) {
            referenceTimeStamp = new Timestamp(System.currentTimeMillis()).getTime();
            this.timeBasedTimestamp = true;
        } else {
            referenceTimeStamp = 0;
            this.timeBasedTimestamp = false;
        }
    }

    /**
     * Get the number of milliseconds since the first time the Stream object was
     * created
     *
     * @return long value of the timestamp
     */
    private long getCurrentTimestamp() {
        long timestamp = (new Timestamp(System.currentTimeMillis()).getTime() - referenceTimeStamp);
        if (!this.timeBasedTimestamp) {
            timestamp = this.referenceTimeStamp + 1;
            ++this.referenceTimeStamp;
        }
        return timestamp;
    }

    /**
     * Write a 2d arrays containing incoming data points into the current stream
     *
     * @param incomingData
     */
    public void writeToStream(double[][] incomingData) {
        for (double[] data : incomingData) {
            this.writeToStream(data);
        }
    }

    /**
     * Write a single incoming data point into the stream
     *
     * @param incomingData
     */
    public void writeToStream(double[] incomingData) {
        // Compute the current time stamp since the the stream was first created
        long currentTimeStamp = getCurrentTimestamp();

        // Create data point object that has the time stamp and the value vector
        DataPoint newPoint = new DataPoint(currentTimeStamp, new DoubleMatrix(incomingData));

        // Add the data point to the data stream
        dataStream.add(newPoint);
    }

    /**
     * Read multiple data points from the stream and returned them in a LinkedList.
     * The data points are removed from the stream at the same itme
     * @param windowSize size of the window that contains the data points from the stream.
     * If the stream uses a count-based timestamp, the window size indicates the number
     * of data points that is going to be read from the stream. If the stream uses a
     * time-based timestamp, the window size indicates the time interval that the data
     * points will be retrieved from the data stream with reference to the timestamp of
     * the oldest data point in the stream.
     * @return a linked list containing all the data points in the window
     */
    public LinkedList<DataPoint> readFromStream(int windowSize) {

        LinkedList<DataPoint> dataPoints = new LinkedList();

        if (timeBasedTimestamp) {
            // Get the data points from the stream when the stream timestamp
            // is time-based. All data points in the time interval windowSize
            // starting from the timestamp of the latest data point in the stream
            // will be retrieved
            if (!this.isEmpty()) {
                DataPoint p = this.peek();
                long timestampThreshold = p.getTimestamp() + windowSize;

                while ((!this.isEmpty())
                        && (this.peek().getTimestamp() < timestampThreshold)) {
                    dataPoints.addLast(this.readFromStream());
                }
            }
        } else {
            // Get the data points from the stream when the stream timestamp
            // is count-based. The windowSize indicates the number of data points
            // that is going to be retrieved from the stream
            while (windowSize > 0) {
                if (!this.isEmpty()) {
                    dataPoints.addLast(this.readFromStream());
                    --windowSize;
                } else {
                    break;
                }
            }
        }

        return dataPoints;
    }

    /**
     * Get the oldest data point in the stream and remove it from the stream at the
     * same time.
     * @return the oldest data point in the data stream
     */
    public DataPoint readFromStream() {
        return this.dataStream.poll();
    }

    /**
     * Take a peek at the data stream to see the oldest DataPoint in the stream
     * without removing it.
     *
     * @return the oldest data point in the stream.
     */
    public DataPoint peek() {
        return this.dataStream.peek();
    }

    /**
     * Get the size of the data stream
     *
     * @return integer value indicating the number of data points in the stream
     */
    public int size() {
        return this.dataStream.size();
    }

    /**
     * Check if the data stream is empty or not
     *
     * @return true if the stream is empty, else return false
     */
    public boolean isEmpty() {
        return this.dataStream.isEmpty();
    }
}
