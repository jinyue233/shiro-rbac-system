package name.ealen.interfaces.facade;

import name.ealen.domain.vo.Resp;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 测试shiro重定向打开新窗口页面,重定向只能在浏览器本窗口打开页面，如果要打开新页面用window.open函数。
 *
 * 关于重定向的cookie设置结论：
 * 1，
 *
 */
@RestController
public class ShiroRederectFacade {
    int i;

    // 【结论1】重定向添加的cookie设置进主机domain(若Host为shiro.com，则返回给浏览器后的cookie属于shiro.com这个domain)
    @GetMapping(value = "/open/api/redirect")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Cookie cookie = new Cookie("name", "jinyue233");
        response.addCookie(cookie);
        WebUtils.issueRedirect(request, response, "http://cas.com:8088/login");
    }

    // 【结论3】重定向添加的cookie若设置domain为其他domain（主机Host为shiro.com，设置其他domain为cas.com，
    // 此时cookie可以返回给浏览器，但会被浏览器bolcked，提示blocked信息，因此浏览器访问访问cas.com不会带着此cookie过去）
    @GetMapping(value = "/open/api/redirect2")
    public void redirect2(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Cookie cookie = new Cookie("name", "jinyue233");
        cookie.setDomain("cas.com");
        response.addCookie(cookie);
        WebUtils.issueRedirect(request, response, "http://cas.com:8088/login");
    }

    // 【结论3】重定向添加的cookie若设置domain为其他domain（主机Host为shiro.com，设置其他domain为cas.com，并设置path为"/"，
    // 此时cookie可以返回给浏览器，但会被浏览器bolcked，提示blocked信息，因此浏览器访问访问cas.com不会带着此cookie过去）
    @GetMapping(value = "/open/api/redirect3")
    public void redirect3(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Cookie cookie = new Cookie("name", "jinyue233");
        cookie.setDomain("cas.com");
        cookie.setPath("/");
        response.addCookie(cookie);
        WebUtils.issueRedirect(request, response, "http://cas.com:8088/login");
    }

    // 【结论3】重定向添加的cookie若设置domain为其他domain（主机Host为shiro.com，设置其他domain为cas.com，并设置path为"/"，
    // 此时cookie可以返回给浏览器，但会被浏览器bolcked，提示blocked信息，因此浏览器访问访问cas.com不会带着此cookie过去）
    @GetMapping(value = "/open/api/testCookieOveride")
    public void testCookieOveride(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Cookie cookie = new Cookie("name", "jinyue" + i++);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping(value = "/open/api/testCookieDelete")
    public void testCookieDelete(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Cookie cookie = new Cookie("name", "jinyue" + i++);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    // 总结：可以通过后端java直接重定向到vue的页面（路由），利用这个可以实现单点登录从查询参数中拿到tickey再去cas认证，成功后返回token给vue前端即可
    @GetMapping(value = "/open/api/testRedirectToVuePage")
    public void testRedirectToVuePage(HttpServletRequest request, HttpServletResponse response) throws Exception{
        System.out.println("redirectTo:http://127.0.0.1:9528/#/recruit/enterprice");
        WebUtils.issueRedirect(request, response, "http://127.0.0.1:9528/#/recruit/enterprice");
    }
}
