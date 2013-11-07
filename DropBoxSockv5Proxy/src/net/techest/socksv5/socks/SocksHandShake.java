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
 *  Created on : 2011-8-20, 下午4:56:05
 *  Author     : princehaku
 */

package net.techest.socksv5.socks;

import java.io.IOException;

import net.techest.socksv5.auth.AuthException;
import net.techest.socksv5.enu.EAuthType;

public class SocksHandShake {
	/**版本
	 * 
	 */
	int VER=5;
	/**接收时的METHODS的长度
	 * 
	 */
	int NMETHODS;
	/**验证方式
	 * 
	 */
	EAuthType METHODS=EAuthType.NONE;
	
	SocksSocket s;


	public SocksHandShake(SocksSocket sck) {
		this.s=sck;
	}

	
	public void accept() throws AuthException, IOException {
		//接收客户端请求消息
		byte[] content=this.s.pull(1);
		this.VER=content[0];
		content=this.s.pull(1);
		this.NMETHODS=content[0];
		content=this.s.pull(this.NMETHODS);
		int authTypeInt=content[0];
		this.METHODS.valueOf(authTypeInt);

		//发送确认回复
		this.s.push(0x05);
		if(authTypeInt==0){
			this.s.push(0x00);
		}else{
			throw new AuthException("Not Support " +authTypeInt+"  with "+ this.METHODS.toString());
		}
	}

	public SocksSocket getHandler() {
		return s;
	}
	
	@Override
	public String toString(){
		return "Ver:"+this.VER+" AuthType: "+this.METHODS.toString();
	}
}
