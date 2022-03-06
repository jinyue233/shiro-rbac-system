package name.ealen.infrastructure.filters;

import name.ealen.infrastructure.config.JwtToken;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class JwtFilter extends AuthenticatingFilter {


    @Override
    protected  AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception{
        return new JwtToken(WebUtils.getCleanParam(request, "token"));
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return executeLogin(request, response);
    }
}
