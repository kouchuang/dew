/*
 * Copyright 2022. the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package group.idealworld.dew.core.web.interceptor;

import com.ecfront.dew.common.$;
import com.ecfront.dew.common.StandardCode;
import group.idealworld.dew.Dew;
import group.idealworld.dew.core.DewContext;
import group.idealworld.dew.core.web.error.ErrorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * IdentInfo拦截器.
 *
 * @author gudaoxuri
 * @author gjason
 */
public class IdentInfoHandlerInterceptor implements AsyncHandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentInfoHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (request.getHeader(Dew.dewConfig.getSecurity().getIdentInfoFlag()) == null) {
            ErrorController.error(request, response, Integer.parseInt(StandardCode.BAD_REQUEST.toString()),
                    "The request is missing [" + Dew.dewConfig.getSecurity().getIdentInfoFlag() + "] in header", AuthException.class.getName());
            return false;
        }
        var optInfo = $.json.toObject($.security.decodeBase64ToString(request.getHeader(Dew.dewConfig.getSecurity().getIdentInfoFlag()), StandardCharsets.UTF_8),
                DewContext.getOptInfoClazz());
        var optInfoOpt = Optional.of(optInfo);
        var token = optInfo.getToken();
        var tokenKind = optInfo.getTokenKind();
        DewContext context = new DewContext();
        context.setId($.field.createUUID());
        context.setSourceIP(Dew.Util.getRealIP(request));
        context.setRequestUri(request.getRequestURI());
        context.setToken(token);
        context.setInnerOptInfo(optInfoOpt);
        context.setTokenKind(tokenKind);
        DewContext.setContext(context);
        return true;
    }

}
