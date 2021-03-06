package cn.junengxiong.config.shiro_config;

import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import cn.junengxiong.bean.User;
import cn.junengxiong.service.UserService;

/**
 * 自定义登录权限认证
 * 
 * @ClassName: MyShiroRealm
 * @Description TODO 
 * @version
 * @author jh
 * @date 2019年8月27日 下午4:12:40
 */

public class MyShiroRealm extends AuthorizingRealm {
    @Autowired
	@Lazy
    UserService userService;


    /**
     * 权限设置
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (userService == null) {
            userService = (UserService) SpringBeanFactoryUtil.getBeanByName("userServiceImpl");
        }
        System.out.println("进入自定义权限设置方法！");
        String username = (String) principals.getPrimaryPrincipal();
        // 从数据库或换村中获取用户角色信息
        User user = userService.findByUsername(username);
        // 获取用户角色
        Set<String> roles = user.getRole();
        // 获取用户权限
        Set<String> permissions = user.getPermission();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        // 设置权限
        simpleAuthorizationInfo.setStringPermissions(permissions);
        // 设置角色
        simpleAuthorizationInfo.setRoles(roles);

        return simpleAuthorizationInfo;
    }

    /**
     * 身份验证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        System.out.println("进入自定义登录验证方法！");
        if (userService == null) {
            userService = (UserService) SpringBeanFactoryUtil.getBeanByName("userServiceImpl");
        }
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String username = usernamePasswordToken.getUsername();// 用户输入用户名
        User user = userService.findByUsername(username);// 根据用户输入用户名查询该用户
        if (user == null) {
            throw new UnknownAccountException();// 用户不存在
        }
        String password = user.getPassword();// 数据库获取的密码
        // 主要的（用户名，也可以是用户对象（最好不放对象）），资格证书(数据库获取的密码)，区域名称（当前realm名称）
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(username, password, getName());
        //加盐，对比的时候会使用该参数对用户输入的密码按照密码比较器指定规则加盐，加密，再去对比数据库密文
        simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(username));
        return simpleAuthenticationInfo;
    }


    
    
    /**
     * 重写方法,清除当前用户的的 授权缓存
     * @param principals
     */
    public void clearCachedAuthorizationInfo() {
        super.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }

    /**
     * 重写方法，清除当前用户的 认证缓存
     * @param principals
     */
    public void clearCachedAuthenticationInfo() {
        super.clearCachedAuthenticationInfo(SecurityUtils.getSubject().getPrincipals());
    }
    /**
     * 清除某个用户认证和授权缓存
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

    /**
     * 自定义方法：清除所有 授权缓存
     */
    public void clearAllCachedAuthorizationInfo() {
        getAuthorizationCache().clear();
    }

    /**
     * 自定义方法：清除所有 认证缓存
     */
    public void clearAllCachedAuthenticationInfo() {
        getAuthenticationCache().clear();
    }

    /**
     * 自定义方法：清除所有的  认证缓存  和 授权缓存
     */
    public void clearAllCache() {
        clearAllCachedAuthenticationInfo();
        clearAllCachedAuthorizationInfo();
    }
    
    
    
}
