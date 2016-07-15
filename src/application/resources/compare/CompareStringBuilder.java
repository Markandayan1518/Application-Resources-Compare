package application.resources.compare;

public class CompareStringBuilder {

    public static StringBuilder leftSideStringBuilder;
    public static StringBuilder rightSideStringBuilder;
    public static int missingCount;
    public static int extraCount;

    public CompareStringBuilder() {
        leftSideStringBuilder = new StringBuilder();
        rightSideStringBuilder = new StringBuilder();
        missingCount = 0;
        extraCount = 0;
    }
}
