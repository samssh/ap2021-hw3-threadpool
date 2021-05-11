package ir.sharif.math.ap.hw3;

import java.util.Arrays;
import java.util.List;

public class Job {
    private final NewRunnable runnable;
    private final List<String> resources;

    public Job(NewRunnable runnable, String... resources) {
        this.runnable = runnable;
        this.resources = Arrays.asList(resources);
    }

    public NewRunnable getRunnable() {
        return runnable;
    }

    public List<String> getResources() {
        return resources;
    }
}
