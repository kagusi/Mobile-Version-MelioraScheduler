package edu.rochester.meliorascheduler.HttpHandler;

/**
 * Created by Kennedy Agusi on 11/8/2017.
 */

public class WorkPoster<W> implements Runnable {
    private final W mWork;
    private final JobListener<W> mListener;

    public WorkPoster(W work, JobListener<W> listener) {
        mWork = work;
        mListener = listener;
    }

    @Override
    public void run() {
        mListener.someWorkCompleted(mWork);
    }
}
