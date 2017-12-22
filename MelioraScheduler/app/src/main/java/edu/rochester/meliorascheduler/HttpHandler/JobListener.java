package edu.rochester.meliorascheduler.HttpHandler;

/**
 * Created by Kennedy Agusi on 11/8/2017.
 */

public interface JobListener<W> {
    public void someWorkCompleted(W work);

    public void jobComplete();
}