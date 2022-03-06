package name.ealen.infrastructure.config;

import name.ealen.domain.dao.PermissionRepository;
import name.ealen.domain.entity.Permission;
import name.ealen.infrastructure.filters.CustomFormAuthenticationFilter;
import name.ealen.infrastructure.filters.SimpleCORSFilter;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSessionStorageEvaluator;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.servlet.Filter;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by EalenXie on 2019/3/25 15:12.
 */
@Configuration
public class ShiroConfig {

    @Resource
    private PermissionRepository permissionRepository;

    @Resource
    private UserAuthRealm userAuthRealm;

    /**
     * 配置 资源访问策略 . web应用程序 shiro核心过滤器配置
     */
    @Bean
    public ShiroFilterFactoryBean factoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        factoryBean.setLoginUrl("/login");//登录页
        factoryBean.setSuccessUrl("/index");//首页
        factoryBean.setUnauthorizedUrl("/unauthorized");//未授权界面
        // 添加自定义过滤器
        Map<String, Filter> filterMap = factoryBean.getFilters();
        filterMap.put("cors", simpleCORSFilter());//
        filterMap.put("authc", new CustomFormAuthenticationFilter());
        factoryBean.setFilterChainDefinitionMap(setFilterChainDefinitionMap()); //配置 拦截过滤器链

        return factoryBean;
    }

    @Bean
    public SimpleCORSFilter simpleCORSFilter() {
        return new SimpleCORSFilter();
    }

    /**
     * 配置 SecurityManager,可配置一个或多个realm
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        CustomDefaultWebSessionStorageEvaluator defaultSessionStorageEvaluator = new CustomDefaultWebSessionStorageEvaluator();
        /*DefaultWebSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultWebSessionStorageEvaluator();*/
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        DefaultSubjectDAO defaultSubjectDAO = new DefaultSubjectDAO();
        defaultSubjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(defaultSubjectDAO);
        securityManager.setRealm(userAuthRealm);
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdCookie(simpleCookie());
        return sessionManager;
    }



    /**
     * 开启shiro 注解支持. 使以下注解能够生效 :
     * 需要认证 {@link org.apache.shiro.authz.annotation.RequiresAuthentication RequiresAuthentication}
     * 需要用户 {@link org.apache.shiro.authz.annotation.RequiresUser RequiresUser}
     * 需要访客 {@link org.apache.shiro.authz.annotation.RequiresGuest RequiresGuest}
     * 需要角色 {@link org.apache.shiro.authz.annotation.RequiresRoles RequiresRoles}
     * 需要权限 {@link org.apache.shiro.authz.annotation.RequiresPermissions RequiresPermissions}
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 配置 拦截过滤器链.  map的键 : 资源地址 ;  map的值 : 所有默认Shiro过滤器实例名
     * 默认Shiro过滤器实例 参考 : {@link org.apache.shiro.web.filter.mgt.DefaultFilter}
     */
    private Map<String, String> setFilterChainDefinitionMap() {
        Map<String, String> filterMap = new LinkedHashMap<>();
        //注册 数据库中所有的权限 及其对应url
        List<Permission> allPermission = permissionRepository.findAll();//数据库中查询所有权限
        /*for (Permission p : allPermission) {
            filterMap.put(p.getUrl(), "perms[" + p.getName() + "]");    //拦截器中注册所有的权限
        }*/

        /**
         * 【shiro过滤器路径匹配结论】：
         * 1，只要一旦匹配到对应路径的过滤器，那么后面配置的符合路径匹配的过滤器会失效不会执行，即执行优先匹配到的过滤器，
         *    因此不能把路径全匹配的过滤器放到前面（比如filterMap.put("/**", "authc,cors");），否则会屏蔽后面精确
         *    匹配到的过滤器。
         *    举个例子：
         *    如下配置：
         *    filterMap.put("/**", "authc,cors");
         *    filterMap.put("/logout", "logout");
         *    此时一个/logou请求过来，匹配到的是authc,cors，而logpout过滤器被忽略
         *  2，一个路径匹配到多个shiro过滤器时，如果前面过滤器不返回false，那么这些所有过滤器都会被执行；
         *     如果前面的某个过滤器返回false，那么其后的过滤器不会执行，此时请求也到达不了controoler的requestMapping方法
         *     【QUESTION】为什么只要shiro有一个过滤器返回false，就到达不了controoler的requestMapping方法呢？
         *     【ANSWER】因为只要一个shiro的过滤器返回false，那么就不会再调用shiro的下一个过滤器同时也不会调用servlet后面的过滤器链（Shiro的ProxyFilterChain包装了原始的servlet过滤器链），
         *              所以此时请求也到达不了controoler的requestMapping方法，详见doFilterInternal的doFilterInternal方法的continueChain属性上下代码。
         *
         *  3，如果一个路径匹配到多个shiro过滤器，这些过滤器执行顺序按配置的顺序执行，比如配置是filterMap.put("/**", "authc,cors")，
         *     那么过滤器执行顺序为：authc、cors

         */

        filterMap.put("/static/**", "anon");    //公开访问的资源
        filterMap.put("/open/api/**", "anon");  //公开接口地址
        filterMap.put("/logout", "logout");     //配置登出页,shiro已经帮我们实现了跳转
        // 如果要禁止创建session，那么需要将noSessionCreation至于其他过滤器之前
        filterMap.put("/**", "noSessionCreation, authc, cors");          //所有资源都需要经过验证
        return filterMap;
    }

    /**
     * 凭证匹配 : 指定 加密算法,散列次数
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");   //散列算法:这里使用MD5算法
        hashedCredentialsMatcher.setHashIterations(1024); //散列的次数，比如散列两次，相当于 md5(md5(""))
        return hashedCredentialsMatcher;
    }

    @Bean
    public SimpleCookie simpleCookie() {
        SimpleCookie cookie = new SimpleCookie("customCookidId");
        return cookie;
    }

}
