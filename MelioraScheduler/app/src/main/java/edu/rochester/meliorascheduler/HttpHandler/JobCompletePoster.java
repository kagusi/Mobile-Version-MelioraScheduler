package edu.rochester.meliorascheduler.HttpHandler;

/**
 * Created by Kennedy Agusi on 11/8/2017.
 */

public class JobCompletePoster implements Runnable {
    private final JobListener<?> mListener;

    public JobCompletePoster(JobListener<?> listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        mListener.jobComplete();
    }
}
