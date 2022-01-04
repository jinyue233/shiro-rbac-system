package name.ealen.interfaces.facade;

import name.ealen.domain.vo.Resp;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 测试shiro重定向打开新窗口页面
 */
@RestController
public class ShiroRederectFacade {

    @GetMapping(value = "/open/api/redirect")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws Exception{
        WebUtils.issueRedirect(request, response, "/login");
    }
}
