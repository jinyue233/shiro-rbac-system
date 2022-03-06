package name.ealen.infrastructure.config;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSessionStorageEvaluator;


public class CustomDefaultWebSessionStorageEvaluator extends DefaultWebSessionStorageEvaluator {

    /**
     * 由于用表单认证过滤器时当登录成功会创建session， TODO 为何会这样
     * 不覆盖isSessionStorageEnabled方法的话那么subject.getSession(false) != null判断会返回true，
     * 从而导致session与cookie绑定生效。这里覆盖后将不会将subject的认证成功属于与session绑定
     * @param subject
     * @return
     */
    @Override
    public boolean isSessionStorageEnabled(Subject subject) {
        return false;
    }
}
