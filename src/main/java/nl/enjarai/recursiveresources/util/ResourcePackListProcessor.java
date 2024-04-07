package nl.enjarai.recursiveresources.util;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import nl.enjarai.recursiveresources.gui.ResourcePackFolderEntry;

public class ResourcePackListProcessor {
    private static String name(TransferableSelectionList.PackEntry entry) {
        return entry == null ? "<INVALID>" : entry.pack.getTitle().getString();
    }

    private static String description(TransferableSelectionList.PackEntry entry) {
        return entry == null ? "<INVALID>" : entry.pack.getDescription().getString();
    }

    private static String nameSort(TransferableSelectionList.PackEntry entry, boolean reverse) {
        String pfx1 = !reverse ? "a" : "z";
        String pfx2 = !reverse ? "b" : "y";
        String pfx3 = !reverse ? "x" : "a";

        if (entry instanceof ResourcePackFolderEntry folder) {
            return (folder.isUp ? pfx1 : pfx2) + name(folder); // sort folders first
        } else {
            return pfx3 + name(entry);
        }
    }

    public static final Comparator<TransferableSelectionList.PackEntry> sortAZ = (entry1, entry2) -> String.CASE_INSENSITIVE_ORDER.compare(nameSort(entry1, false), nameSort(entry2, false));
    public static final Comparator<TransferableSelectionList.PackEntry> sortZA = (entry1, entry2) -> -String.CASE_INSENSITIVE_ORDER.compare(nameSort(entry1, true), nameSort(entry2, true));

    private final Runnable callback;
    private int pauseCallback;

    private Comparator<TransferableSelectionList.PackEntry> sorter;
    private Pattern textFilter;
    private String lastTextFilter;

    public ResourcePackListProcessor(Runnable callback) {
        this.callback = callback;
    }

    public void pauseCallback() {
        ++pauseCallback;
    }

    public void resumeCallback() {
        if (pauseCallback > 0) {
            --pauseCallback;
            tryRunCallback();
        }
    }

    private void tryRunCallback() {
        if (pauseCallback == 0) {
            callback.run();
        }
    }

    public void setSorter(Comparator<TransferableSelectionList.PackEntry> comparator) {
        this.sorter = comparator;
        tryRunCallback();
    }

    public void setFilter(String text) {
        text = StringUtils.trimToNull(text);

        if (!Objects.equals(text, lastTextFilter)) {
            lastTextFilter = text;
            textFilter = text == null ? null : Pattern.compile("\\Q" + text.replace("*", "\\E.*\\Q") + "\\E", Pattern.CASE_INSENSITIVE);
            tryRunCallback();
        }
    }

    public void apply(List<TransferableSelectionList.PackEntry> sourceList, List<TransferableSelectionList.PackEntry> extraList, List<TransferableSelectionList.PackEntry> targetList) {
        targetList.clear();
        addMatching(sourceList, targetList);

        if (extraList != null) {
            addMatching(extraList, targetList);
        }

        if (sorter != null) {
            targetList.sort(sorter);
        }
    }

    private void addMatching(List<TransferableSelectionList.PackEntry> source, List<TransferableSelectionList.PackEntry> target) {
        for (TransferableSelectionList.PackEntry entry : source) {
            if (checkFilter(name(entry)) || checkFilter(description(entry))) {
                target.add(entry);
            }
        }
    }

    private boolean checkFilter(String entryText) {
        return textFilter == null || entryText.equals(ResourcePackFolderEntry.UP_TEXT) || textFilter.matcher(entryText.toLowerCase(Locale.ENGLISH)).find();
    }
}
