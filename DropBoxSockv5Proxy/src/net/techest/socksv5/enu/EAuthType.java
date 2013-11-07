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
 *  Created on : 2011-8-20, 下午4:43:00
 *  Author     : princehaku
 */

package net.techest.socksv5.enu;

/** 验证方式
 *
 * 
 */
public enum EAuthType {
	/** X'00' 无验证需求
	 * 
	 */
	NONE,
	/** X'01' 通用安全服务应用程序接口(GSSAPI)
	 * 
	 */
	GSSAPI,
	/**X'02' 用户名/密码(USERNAME/PASSWORD)
	 * 
	 */
	USER_PWD,
	/**X'03' 至 X'7F' IANA 分配(IANA ASSIGNED)
	 * 
	 */
	IANA,
	/**X'80' 至 X'FE' 私人方法保留(RESERVED FOR PRIVATE METHODS)
	 * 
	 */
	RESERVED,
	/**X'FF' 无可接受方法(NO ACCEPTABLE METHODS)
	 * 
	 */
	NO_ACCEPTABLE;
	
	public EAuthType valueOf(int authType){
			EAuthType e=EAuthType.NONE;
			
			if(authType==0){
				e=EAuthType.NONE;
			}
			if(authType==1){
				e=EAuthType.GSSAPI;
			}
			if(authType==2){
				e=EAuthType.USER_PWD;
			}
			if(authType>=3&&authType<=0x7F){
				e=EAuthType.IANA;
			}
			if(authType>=0x80&&authType<=0xFE){
				e=EAuthType.IANA;
			}
			if(authType==0xFF){
				e=EAuthType.NO_ACCEPTABLE;
			}
			
			return e;
		}
	
}
