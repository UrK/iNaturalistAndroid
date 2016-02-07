package org.tatzpiteva.golan;

import java.util.ArrayList;
import java.util.List;

public class LaunchScreenCarouselConfig {
    public static class Pic {
        private int id;
        private String name;
        private String url;
        private Integer order;

        Pic(int id, String name, String url, Integer order) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.order = order;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public Integer getOrder() {
            return order;
        }
    }

    private List<Pic> pics;

    LaunchScreenCarouselConfig() {
        pics = new ArrayList<>();
    }

    public void addPic(Pic pic) {
        pics.add(pic);
    }

    public List<Pic> getPics() {
        return pics;
    }
}
