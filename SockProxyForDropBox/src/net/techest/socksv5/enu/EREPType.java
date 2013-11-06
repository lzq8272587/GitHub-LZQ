/*  Copyright 2010 princehaku
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on : 2011-8-24, 上午11:19:56
 *  Author     : princehaku
 */

package net.techest.socksv5.enu;

public class EREPType {
	public final static int R_SUCCESS = 0;
	public final static int R_SOCKS_GENERAL_FAILD = 0x01;
	public final static int R_CONNECT_NOT_ALLOW = 0x02;
	public final static int R_NETWOK_UNREACHABLE = 0x03;
	public final static int R_HOST_UNREACHABLE = 0x04;
	public final static int R_CONNECT_REFUSE = 0x05;
	public final static int R_TTL_EXPIRE = 0x06;
	public final static int R_CMD_NOT_SUPPORT = 0x07;
	public final static int R_ADDTYPE_NOT_SUPPORT = 0x08;

}
