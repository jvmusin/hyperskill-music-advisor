package advisor;

import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class Page<T> {
    private final int pageCount;
    private final Function<Integer, List<T>> loadContent;
    private final boolean separateResults;
    private List<T> content;
    private int number = 1;

    public Page(int pageCount, boolean separateResults, List<T> firstPageContent, Function<Integer, List<T>> loadContent) {
        this.pageCount = pageCount;
        this.loadContent = loadContent;
        this.separateResults = separateResults;
        this.content = firstPageContent;
    }

    public void previous() {
        if (number == 1) {
            System.out.println("No more pages.");
            return;
        }
        content = loadContent.apply(--number);
        print();
    }

    public void next() {
        if (number + 1 > pageCount) {
            System.out.println("No more pages.");
            return;
        }
        content = loadContent.apply(++number);
        print();
    }

    public void print() {
        content.forEach(item -> {
            System.out.println(item);
            if (separateResults) System.out.println();
        });
        System.out.printf("---PAGE %d OF %d---%n", number, pageCount);
    }
}
