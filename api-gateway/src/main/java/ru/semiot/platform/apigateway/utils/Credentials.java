/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.semiot.platform.apigateway.utils;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Entity
@Table(name = "credentials")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Credentials.findAll", query = "SELECT c FROM Credentials c"),
    @NamedQuery(name = "Credentials.findById", query = "SELECT c FROM Credentials c WHERE c.id = :id"),
    @NamedQuery(name = "Credentials.findByLogin", query = "SELECT c FROM Credentials c WHERE c.login = :login"),
    @NamedQuery(name = "Credentials.findByPassword", query = "SELECT c FROM Credentials c WHERE c.password = :password"),
    @NamedQuery(name = "Credentials.findByRole", query = "SELECT c FROM Credentials c WHERE c.role = :role"),
    @NamedQuery(name = "Credentials.Count", query = "SELECT COUNT(c) FROM Credentials c")})
public class Credentials implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "id")
  private Integer id;
  @Size(max = 40)
  @Column(name = "login")
  private String login;
  @Size(max = 20)
  @Column(name = "password")
  private String password;
  @Size(max = 10)
  @Column(name = "role")
  private String role;

  public Credentials() {
  }

  public Credentials(Integer id, String login, String password, String role) {
    this.id = id;
    this.login = login;
    this.password = password;
    this.role = role;
  }

  public Credentials(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void updateInfo(String login, String password, String role) {
    this.login = login;
    this.password = password;
    this.role = role;
  }

  public boolean needUpdate(Credentials c) {
    return Objects.equals(this.id, c.getId()) && (!this.login.equals(c.login)
        || !this.password.equals(c.password) || !this.role.equals(c.role));
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Credentials)) {
      return false;
    }
    Credentials other = (Credentials) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return id.toString() + " " + login + " " + password + " " + role;
  }

}
