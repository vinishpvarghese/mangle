/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.model;

import lombok.Data;

/**
 * @author bkharanam
 *
 * Pojo for mapping the host lists that are returned from the VC api getHost
 * call
 */
@Data
public class Host {
    private String host;
    private String name;
    private String connection_state;
    private Integer power_state;
}