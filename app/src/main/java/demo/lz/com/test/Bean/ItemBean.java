package demo.lz.com.test.Bean;

/**
 * Created by Administrator on 2018/7/9.
 */

public class ItemBean  {
    private boolean state=false;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Url:::::"+url+"       State:::::"+state;
    }
}
