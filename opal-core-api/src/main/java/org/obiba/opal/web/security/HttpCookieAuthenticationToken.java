/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import org.apache.shiro.authc.AuthenticationToken;

public class HttpCookieAuthenticationToken implements AuthenticationToken {

  private static final long serialVersionUID = 4520790559763117320L;

  private final String sessionId;

  private final String url;

  private final String hash;

  public HttpCookieAuthenticationToken(String sessionId, String url, String hash) {
    this.sessionId = sessionId;
    this.url = url;
    this.hash = hash;
  }

  @Override
  public Object getPrincipal() {
    return getSessionId();
  }

  @Override
  public Object getCredentials() {
    return getHash();
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUrl() {
    return url;
  }

  public String getHash() {
    return hash;
  }

}
