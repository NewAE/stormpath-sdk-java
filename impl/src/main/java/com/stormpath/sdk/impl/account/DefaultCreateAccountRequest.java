/*
 * Copyright 2013 Stormpath, Inc.
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
package com.stormpath.sdk.impl.account;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountOptions;
import com.stormpath.sdk.account.CreateAccountRequest;
import com.stormpath.sdk.lang.Assert;

/**
 * @since 0.9
 */
public class DefaultCreateAccountRequest implements CreateAccountRequest {

    private final Account account;

    private final Boolean registrationWorkflowEnabled;

    private final AccountOptions options;

    public DefaultCreateAccountRequest(Account account, Boolean registrationWorkflowEnabled, AccountOptions options) {
        Assert.notNull(account, "Account cannot be null.");
        this.account = account;
        this.registrationWorkflowEnabled = registrationWorkflowEnabled;
        this.options = options;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public boolean isRegistrationWorkflowOptionSpecified() {
        return this.registrationWorkflowEnabled != null;
    }

    @Override
    public boolean isRegistrationWorkflowEnabled() throws IllegalStateException {
        if (this.registrationWorkflowEnabled == null) {
            throw new IllegalStateException("registrationWorkflowEnabled has not been configured. Use the isRegistrationWorkflowOptionSpecified method to check first before invoking this method.");
        }
        return this.registrationWorkflowEnabled;
    }

    @Override
    public boolean isAccountOptionsSpecified() {
        return this.options != null;
    }

    @Override
    public AccountOptions getAccountOptions() {
        if (this.options == null) {
            throw new IllegalStateException("accountOptions has not been configured. Use the isAccountOptionsSpecified method to check first before invoking this method.");
        }
        return this.options;
    }
}
