package org.tatzpiteva.golan;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.Collection;

/**
 * Whole configuration as received from the server
 */
public class Config {

    /** Golan Wildlife project */
    private static final int DEFAULT_AUTO_USER_JOIN_PROJECT_ID = 4527;

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
        public int id;
        public String title;
        public String slug;
        public Double latitude;
        public Double longitude;
        public Float zoomLevel;
        public SmartFlag smart_flag;
        public SmartFlag menu_flag;
        public Boolean detailsRetrieved;

        public AutoProject() {
            detailsRetrieved = false;
        }

        protected AutoProject(Parcel in) {
            id = in.readInt();
            title = in.readString();
            slug = in.readString();
            smart_flag = SmartFlag.fromInt(in.readInt());
            menu_flag = SmartFlag.fromInt(in.readInt());
            latitude = in.readDouble();
            longitude = in.readDouble();
            zoomLevel = in.readFloat();
            detailsRetrieved = (in.readInt() != 0);
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

            parcel.writeInt(id);
            parcel.writeString(title);
            parcel.writeString(slug);
            parcel.writeInt(smart_flag.getValue());
            parcel.writeInt(menu_flag.getValue());
            parcel.writeDouble(latitude);
            parcel.writeDouble(longitude);
            parcel.writeFloat(zoomLevel);
            parcel.writeInt(detailsRetrieved ? 1 : 0);
        }

        @Override
        public String toString() {
            return "<Config.AutoProject: id=" + id + ", title=" + title + ", smart_flag=" + smart_flag + ">";
        }

    }

    private Collection<AutoProject> autoProjects;
    private int autoUserJoinProject;
    private JSONObject autoUserJoinProjectDetails;

    @Override
    public String toString() {
        return "<Config: autoProjects=" + autoProjects + ">";
    }

    public Config() {
        this(null);
    }

    public Config(Collection<AutoProject> autoProjects) {
        this(autoProjects, DEFAULT_AUTO_USER_JOIN_PROJECT_ID);
    }

    public Config(Collection<AutoProject> autoProjects, int autoUserJoinProjectId) {
        this.autoUserJoinProject = autoUserJoinProjectId;
        this.autoProjects = autoProjects;
    }

    public Collection<AutoProject> getAutoProjects() {
        return autoProjects;
    }

    public int getAutoUserJoinProject() {
        return autoUserJoinProject;
    }

    @Nullable
    public JSONObject getAutoUserJoinProjectDetails() {
        return autoUserJoinProjectDetails;
    }

    public void setAutoUserJoinProjectDetails(@Nullable JSONObject projDetails) {
        autoUserJoinProjectDetails = projDetails;
    }
}
