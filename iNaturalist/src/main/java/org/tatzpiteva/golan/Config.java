package org.tatzpiteva.golan;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;

/**
 * Whole configuration as received from the server
 */
public class Config {

    public enum SmartFlag {
        NOT_CHECKED(0),
        DEFAULT_READ_WRITE(1),
        DEFAULT_READ_ONLY(2);

        private final int value;

        SmartFlag(int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }

        public static SmartFlag fromInt(int value) {
            for (SmartFlag type : SmartFlag.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return SmartFlag.NOT_CHECKED;
        }
    }

    /**
     * Single project which appears in configuration
     */
    public static class AutoProject implements Parcelable {
        public String id;
        public String title;
        public String slug;
        public SmartFlag smart_flag;
        public SmartFlag menu_flag;

        public AutoProject() { }

        protected AutoProject(Parcel in) {
            id = in.readString();
            title = in.readString();
            slug = in.readString();
            smart_flag = SmartFlag.fromInt(in.readInt());
            menu_flag = SmartFlag.fromInt(in.readInt());
        }

        public static final Creator<AutoProject> CREATOR = new Creator<AutoProject>() {
            @Override
            public AutoProject createFromParcel(Parcel in) {
                return new AutoProject(in);
            }

            @Override
            public AutoProject[] newArray(int size) {
                return new AutoProject[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

            parcel.writeString(id);
            parcel.writeString(title);
            parcel.writeString(slug);
            parcel.writeInt(smart_flag.getValue());
            parcel.writeInt(menu_flag.getValue());
        }
    }

    Collection<AutoProject> autoProjects;
}
