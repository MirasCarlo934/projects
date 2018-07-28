package bm.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import bm.main.Maestro;
import bm.main.UserPropertyManager;
import bm.main.repositories.CIRRepository;
import bm.main.repositories.DeviceRepository;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.PropertyMode;
import bm.tools.IDGenerator;
import bm.tools.VMCipher;

@Controller
//@PropertySource({"file:configuration/bm.properties", "file:configuration/user.properties"})
@SessionAttributes("user.pwd") //injectable
//@SessionAttributes("${user.pwd.key}") //injectable
@RequestMapping("/")
public class BaseController extends AbstController {
//	@Autowired
//	private ComponentRepository cr;
//	@Autowired
//	private VMCipher cipher; //injectable
//	@Autowired
//	private UserPropertyManager userPropManager;
//	@Autowired
//	private CIRRepository cirr;
	
	public BaseController(@Value("${log.domain.ui}") String logDomain, @Autowired VMCipher cipher, 
			@Autowired UserPropertyManager userPropManager) {
		super(logDomain, BaseController.class.getSimpleName());
	}
	
	@RequestMapping("/")
	public String root(Model model, HttpServletRequest request, HttpServletResponse response) {
		String userPwd = userPropManager.getUserPwd();
		if(userPwd == null || userPwd.isEmpty()) {
			LOG.debug("Dispatching view 'register'");
			return "register";
//		} else if(getUserPwdFromCookie(request) != null && getUserPwdFromCookie(request).equals(userPwd)) {
		} else if(decryptUserPwdInModel(model) != null && decryptUserPwdInModel(model).equals(userPwd)) {
			LOG.debug("Dispatching view 'home'");
			return "home";
		}
		else {
			LOG.debug("Dispatching view 'login'");
//			addUserPwdCookie(response, "");
			setUserPwdInModel(model, "");
			return "login";
		}
	}
	
//	@RequestMapping("/login")
//	public String login(Model model) {
//		LOG.debug("Dispatching view 'login'");
//		setUserPwdInModel(model, "");
//		return "login";
//	}
	
	@RequestMapping("/register")
	public String register(@RequestParam(value="pwd", required=true) String pwd, 
			@RequestParam(value="pwd2", required=true) String confirm_pwd, Model model, 
			HttpServletResponse response) {
		if(userPropManager.getUserPwd() != null) {
			LOG.debug("Registration is invalid when the user has already set his/her password!");
			return "error";
		}
		LOG.debug("Registration request accepted!");
		if(pwd.equals(confirm_pwd)) {
			LOG.debug("VM password update requested. Updating user.properties file!");
			userPropManager.setUserPwd(pwd);
			setUserPwdInModel(model, pwd);
			try {
				userPropManager.store();
				LOG.info("VM password updated!");
			} catch (IOException e) {
				LOG.error("Cannot update user.properties file!", e);
				model.addAttribute("error", "Cannot save new VM password! Please contact Symphony Customer "
						+ "Care.");
				return "register";
			}
			LOG.debug("Registration successful. Dispatching view 'home'");
			return "home";
		} else {
			model.addAttribute("error", "Passwords entered do not match!");
			return "register";
		}
	}
	
	/**
	 * <b><i>AJAX-accessed request mapping</b></i>
	 * <br><br>
	 * Processes user login
	 * 
	 * @param pwd the password entered by the user
	 * @param model the model
	 * @return The home template if successful, a notification with an error message otherwise
	 */
	@RequestMapping("/login")
	public String login(@RequestParam(value="pwd", required=true) String pwd, Model model, 
			HttpServletResponse httpResponse) {
		LOG.debug("Login requested!");
		String userPwd = userPropManager.getUserPwd();
		if(userPwd.equals(pwd)) {
			if(model.containsAttribute("redirect")) { //if logged in from a specific request mapping
				return redirect(model, model.asMap().get("redirect").toString());
			} else { //general login from root request mapping
				LOG.debug("Login successful! Redirecting to home page...");
				setUserPwdInModel(model, pwd);
				return redirect(model, "home");
			}
		} else {
			LOG.debug("Login insuccessful!");
			return notify("error", "Invalid password!", model);
		}
	}
	
	@RequestMapping("/logout")
	public String logout(Model model) {
		LOG.debug("Logout requested!");
		removeUserPwdInModel(model);
		return "login";
	}
	
	@RequestMapping("/home")
	public String home(@RequestParam(value="pwd", required=false, defaultValue="") String pwd, 
			@RequestParam(value="admin", required=false) boolean admin, Model model, 
			HttpServletRequest request, HttpServletResponse response) {
		LOG.debug("Requested 'home' view access...");
		String userPwd = userPropManager.getUserPwd();
		
		LOG.debug("Dispatching view 'home'");
		addUserPwdCookie(response, userPwd);
		return authenticate(pwd, "/home", model);
//		if(authenticate(pwd, "/home", model) == "true" | authenticate(model, "/home") == "true") {
////		if(authenticate(getUserPwdFromCookie(request))) {
//			LOG.debug("Dispatching view 'home'");
//			addUserPwdCookie(response, userPwd); //stores password in cookie
//			return "home";
//		} else {
////			LOG.debug("Dispatching view 'login'");
////			model.addAttribute("error", "Please log-in to continue!");
////			return "login";
//			return requireLogin(model, "home");
//		}
	}
}
