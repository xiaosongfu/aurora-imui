package imui.jiguang.cn.imuikit.utils;

import org.junit.Assert;
import org.junit.Test;

import cn.jiguang.imui.utils.HyperLinkParseUtil;

/**
 * @author fuxiaosong
 * @version 1.0.0
 * @since 2017年09月27日
 */
public class TestHyperLinkParseUtil {
    @Test
    public void parseAHrefLinksTest(){
        Object[][] result = HyperLinkParseUtil.parseAHrefLinks("[link url=\"https://www.baidu.com/?tn=94216471_s_hao_pg\"]hahaha[/link]");
        Assert.assertEquals("hahaha", (String) result[0][0]);
    }
}
