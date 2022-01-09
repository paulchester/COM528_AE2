package org.solent.com504.oodd.cart.spring.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.solent.com504.oodd.cart.dao.impl.UserRepository;
import org.solent.com504.oodd.bank.model.dto.CreditCard;
import org.solent.com504.oodd.cart.model.dto.CardType;
import org.solent.com504.oodd.cart.model.dto.Address;
import org.solent.com504.oodd.cart.model.dto.User;
import org.solent.com504.oodd.cart.model.dto.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class UserAndLoginController {

    final static Logger LOG = LogManager.getLogger(UserAndLoginController.class);

    @Autowired
    UserRepository userRepository;

    private User getSessionUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setUsername("anonymous");
            sessionUser.setUserRole(UserRole.ANONYMOUS);
            session.setAttribute("sessionUser", sessionUser);
        }
        return sessionUser;
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(Model model,
            HttpSession session) {

        // logout of session and clear
        session.invalidate();

        return "redirect:/home";
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET})
    @Transactional
    public String login(
            Model model,
            HttpSession session) {
        String message = "log into site using username";
        String errorMessage = "";

        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (!UserRole.ANONYMOUS.equals(sessionUser.getUserRole())) {
            errorMessage = "user " + sessionUser.getUsername() + " already logged in";
            LOG.warn(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:home";
        }

        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);
        // used to set tab selected
        model.addAttribute("selectedPage", "home");

        return "login";

    }

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    @Transactional
    public String login(@RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "password2", required = false) String password2,
            Model model,
            HttpSession session) {
        String message = "";
        String errorMessage = "";

        LOG.debug("login for username=" + username);

        // get current session modifyUser 
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (!UserRole.ANONYMOUS.equals(sessionUser.getUserRole())) {
            errorMessage = "user " + sessionUser.getUsername() + " already logged in";
            LOG.warn(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:home";
        }

        if (username == null || username.trim().isEmpty()) {
            errorMessage = "you must enter a username";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("message", message);
            return "login";
        }

        List<User> userList = userRepository.findByUsername(username);

        if ("login".equals(action)) {
            //todo find and add modifyUser and test password
            LOG.debug("logging in user username=" + username);
            if (userList.isEmpty()) {
                errorMessage = "cannot find user for username :" + username;
                LOG.warn(errorMessage);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("message", message);
                return "login";
            }
            if (password == null) {
                errorMessage = "you must enter a password";
                LOG.warn(errorMessage);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("message", message);
                return "login";
            }

            User loginUser = userList.get(0);
            if (!loginUser.isValidPassword(password)) {
                model.addAttribute("errorMessage", "invalid username or password");
                model.addAttribute("message", message);
                return "login";
            }

            if (!loginUser.getEnabled()) {
                model.addAttribute("errorMessage", "user account " + username
                        + " is disabled in this system");
                model.addAttribute("message", message);
                return "login";
            }

            message = "successfully logged in user:" + username;
            session.setAttribute("sessionUser", loginUser);

            model.addAttribute("sessionUser", loginUser);

            model.addAttribute("message", message);
            model.addAttribute("errorMessage", errorMessage);
            // used to set tab selected
            model.addAttribute("selectedPage", "home");
            return "redirect:home";
        } else {
            model.addAttribute("errorMessage", "unknown action requested:" + action);
            LOG.error("login page unknown action requested:" + action);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("message", message);
            // used to set tab selected
            model.addAttribute("selectedPage", "home");
            return "redirect:home";
        }
    }

    @RequestMapping(value = "/register", method = {RequestMethod.GET})
    @Transactional
    public String registerGET(@RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "password2", required = false) String password2,
            Model model,
            HttpSession session) {
        String message = "register new user";
        String errorMessage = "";

        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);
        // used to set tab selected
        model.addAttribute("selectedPage", "home");

        return "register";
    }

    @RequestMapping(value = "/register", method = {RequestMethod.POST})
    @Transactional
    public String register(@RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "password2", required = false) String password2,
            Model model,
            HttpSession session) {
        String message = "";
        String errorMessage = "";

        LOG.debug("register new username=" + username);

        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (username == null || username.trim().isEmpty()) {
            errorMessage = "you must enter a username";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("message", message);
            return "register";
        }

        List<User> userList = userRepository.findByUsername(username);

        if ("createNewAccount".equals(action)) {
            if (!userList.isEmpty()) {
                errorMessage = "trying to create user with username which already exists :" + username;
                LOG.warn(errorMessage);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("message", message);
                return "register";
            }
            if (password == null || !password.equals(password2) || password.length() < 8) {
                errorMessage = "you must enter two identical passwords with atleast 8 characters";
                LOG.warn(errorMessage);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("message", message);
                return "register";
            }

            User modifyUser = new User();
            modifyUser.setUserRole(UserRole.CUSTOMER);
            modifyUser.setUsername(username);
            modifyUser.setFirstName(username);
            modifyUser.setPassword(password);
            modifyUser = userRepository.save(modifyUser);

            // if already logged in - keep session modifyUser else set session modifyUser to modifyUser
            // else set session modifyUser the newly created modifyUser (i.e. automatically log in)
            if (UserRole.ANONYMOUS.equals(sessionUser.getUserRole())) {
                session.setAttribute("sessionUser", modifyUser);
                model.addAttribute("sessionUser", modifyUser);
                LOG.debug("log in newly created user=" + modifyUser);
            }

            LOG.debug("createNewAccount created new user user=" + modifyUser);
            message = "enter user details";
            model.addAttribute("modifyUser", modifyUser);
            model.addAttribute("message", message);
            model.addAttribute("errorMessage", errorMessage);
            return "viewModifyUser";
        } else {
            LOG.debug("unknown action " + action);
            model.addAttribute("errorMessage", "unknown action " + action);
            model.addAttribute("message", message);
            return "redirect:home";
        }
    }

    @RequestMapping(value = {"/users"}, method = RequestMethod.GET)
    @Transactional
    public String users(@RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "userId", required = false) String userId,
            Model model,
            HttpSession session) {
        String message = "";
        String errorMessage = "";

        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if ("deleteUser".equals(action)) {
            Long id = Long.parseLong(userId);
            userRepository.deleteById(id);
            message = "Deleted User with ID " + userId;
        }

        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            return "redirect:home";
        }

        List<User> userList = userRepository.findAll();

        model.addAttribute("userListSize", userList.size());
        model.addAttribute("userList", userList);
        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("selectedPage", "users");
        return "users";
    }

    @RequestMapping(value = {"/viewModifyUser"}, method = RequestMethod.GET)
    public String modifyuser(
            @RequestParam(value = "username", required = true) String username,
            Model model,
            HttpSession session) {
        String message = "";
        String errorMessage = "";

        model.addAttribute("selectedPage", "home");

        LOG.debug("get viewModifyUser called for username=" + username);

        // check secure access to modifyUser profile
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (UserRole.ANONYMOUS.equals(sessionUser.getUserRole())) {
            errorMessage = "you must be logged in to access user information";
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:home";
        }

        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            // if not an administrator you can only access your own account info
            if (!sessionUser.getUsername().equals(username)) {
                errorMessage = "security non admin viewModifyUser called for username " + username
                        + "which is not the logged in user =" + sessionUser.getUsername();
                LOG.warn(errorMessage);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("message", message);
                return "redirect:home";
            }
        }

        List<User> userList = userRepository.findByUsername(username);
        if (userList.isEmpty()) {
            LOG.error("viewModifyUser called for unknown username=" + username);
            return "redirect:home";
        }

        User modifyUser = userList.get(0);
        model.addAttribute("modifyUser", modifyUser);

        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);
        return "viewModifyUser";
    }

    @RequestMapping(value = {"/viewModifyUser"}, method = RequestMethod.POST)
    public String updateuser(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "secondName", required = false) String secondName,
            @RequestParam(value = "userRole", required = false) String userRole,
            @RequestParam(value = "userEnabled", required = false) String userEnabled,
            @RequestParam(value = "houseNumber", required = false) String houseNumber,
            @RequestParam(value = "addressLine1", required = false) String addressLine1,
            @RequestParam(value = "addressLine2", required = false) String addressLine2,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "county", required = false) String county,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "postcode", required = false) String postcode,
            @RequestParam(value = "latitude", required = false) String latitude,
            @RequestParam(value = "longitude", required = false) String longitude,
            @RequestParam(value = "telephone", required = false) String telephone,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "password2", required = false) String password2,
            @RequestParam(value = "cardName", required = false) String cardName,
            @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @RequestParam(value = "expiryMonth", required = false) String expiryMonth,
            @RequestParam(value = "expiryYear", required = false) String expiryYear,
            @RequestParam(value = "cardType", required = false) String cardType,
            @RequestParam(value = "action", required = false) String action,
            Model model,
            HttpSession session) {
        String message = "";
        String errorMessage = "";

        LOG.debug("post updateUser called for username=" + username);

        // security check if party is allowed to access or modify this party
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (UserRole.ANONYMOUS.equals(sessionUser.getUserRole())) {
            errorMessage = "you must be logged in to access users information";
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:home";
        }

        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            if (!sessionUser.getUsername().equals(username)) {
                errorMessage = "security viewModifyUser called for non admin username " + username
                        + "which is not the logged in user =" + sessionUser.getUsername();
                model.addAttribute("errorMessage", errorMessage);
                LOG.warn(errorMessage);
                return "redirect:home";
            }
        }

        List<User> userList = userRepository.findByUsername(username);
        if (userList.isEmpty()) {
            errorMessage = "update user called for unknown username:" + username;
            LOG.warn(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:home";
        }

        User modifyUser = userList.get(0);

        if (null != action) {
            switch (action) {
                // update password if requested
                case "updatePassword":
                    if (password == null || !password.equals(password2) || password.length() < 8) {
                        errorMessage = "you must enter two identical passwords with atleast 8 characters";
                        LOG.warn(errorMessage);
                        model.addAttribute("errorMessage", errorMessage);
                        model.addAttribute("message", message);
                        return "viewModifyUser";
                    } else {
                        modifyUser.setPassword(password);
                        modifyUser = userRepository.save(modifyUser);
                        model.addAttribute("modifyUser", modifyUser);
                        message = "password updated for user :" + modifyUser.getUsername();
                        model.addAttribute("message", message);
                        model.addAttribute("errorMessage", errorMessage);
                        return "viewModifyUser";
                    }
                // update user details if requested
                case "updateDetails":
                    // only admin can update modifyUser role and enabled
                    if (UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
                        try {
                            UserRole role = UserRole.valueOf(userRole);
                            modifyUser.setUserRole(role);
                            if (userEnabled != null && "true".equals(userEnabled)) {
                                modifyUser.setEnabled(Boolean.TRUE);
                            } else {
                                modifyUser.setEnabled(Boolean.FALSE);
                            }
                        } catch (Exception ex) {
                            errorMessage = "cannot parse userRole" + userRole;
                            LOG.warn(errorMessage);
                            model.addAttribute("errorMessage", errorMessage);
                            model.addAttribute("message", message);
                            return "redirect:home";
                        }
                    }
                    if (firstName != null) {
                        modifyUser.setFirstName(firstName);
                    }
                    if (secondName != null) {
                        modifyUser.setSecondName(secondName);
                    }
                    message = "Updated details";
                    break;
                // update user address if requested
                case "updateAddress":
                    Address address = new Address();
                    address.setHouseNumber(houseNumber);
                    address.setAddressLine1(addressLine1);
                    address.setAddressLine2(addressLine2);
                    address.setCity(city);
                    address.setCounty(county);
                    address.setCountry(country);
                    address.setPostcode(postcode);
                    address.setMobile(mobile);
                    address.setTelephone(telephone);
                    modifyUser.setAddress(address);
                    message = "Updated address";
                    break;
                // update user payment if requested    
                case "updatePayment":
                    CreditCard creditCard = new CreditCard();
                    creditCard.setName(cardName);
                    creditCard.setCardnumber(cardNumber);
                    String cardEndDate = expiryMonth + "/" + expiryYear;
                    creditCard.setEndDate(cardEndDate);
                    modifyUser.setCreditCard(creditCard);
                    CardType type = CardType.valueOf(cardType);
                    modifyUser.setCardType(type);
                    message = "Updated payment details";
                    break;
                default:
                    break;
            }
        }

        // Save any changed to the repository
        modifyUser = userRepository.save(modifyUser);

        model.addAttribute("modifyUser", modifyUser);

        // add message if there are any 
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("message", message);

        model.addAttribute("selectedPage", "home");

        return "viewModifyUser";
    }

    /*
     * Default exception handler, catches all exceptions, redirects to friendly
     * error page. Does not catch request mapping errors
     */
    @ExceptionHandler(Exception.class)
    public String myExceptionHandler(final Exception e, Model model,
            HttpServletRequest request
    ) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        final String strStackTrace = sw.toString(); // stack trace as a string
        String urlStr = "not defined";
        if (request != null) {
            StringBuffer url = request.getRequestURL();
            urlStr = url.toString();
        }
        model.addAttribute("requestUrl", urlStr);
        model.addAttribute("strStackTrace", strStackTrace);
        model.addAttribute("exception", e);
        //logger.error(strStackTrace); // send to logger first
        return "error"; // default friendly exception message for sessionUser
    }

}
