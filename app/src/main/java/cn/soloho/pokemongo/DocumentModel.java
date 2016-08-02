package cn.soloho.pokemongo;

import com.google.auto.value.AutoValue;

/**
 * Created by solo on 16/8/1.
 */
@AutoValue
public abstract class DocumentModel {

    public abstract String getTitle();
    public abstract String getLink();

    public static Builder builder() {
        return new AutoValue_DocumentModel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setTitle(String newTitle);

        public abstract Builder setLink(String newLink);

        public abstract DocumentModel build();
    }
}
