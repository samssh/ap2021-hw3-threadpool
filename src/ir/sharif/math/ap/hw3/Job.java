package ir.sharif.math.ap.hw3;

import java.util.Arrays;
import java.util.List;

public class Job {
    private final Runnable runnable;
    private final List<String> resources;


    public Job(Runnable runnable, String... resources) {
        this.runnable = runnable;
        this.resources = Arrays.asList(resources);
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public List<String> getResources() {
        return resources;
    }
}
