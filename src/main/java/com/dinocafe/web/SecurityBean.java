package com.dinocafe.web;

import com.dinocafe.SecurityFilter;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

@ManagedBean(name = "security")
@RequestScoped
public class SecurityBean {
  public String getCsrf() {
    return SecurityFilter.token(
        (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true));
  }
}
