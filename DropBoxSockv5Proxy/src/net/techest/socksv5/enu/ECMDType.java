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
 *  Created on : 2011-8-21, 上午9:25:07
 *  Author     : princehaku
 */

package net.techest.socksv5.enu;

/**地址格式
 * 
 * @author princehaku
 *
 */
public class ECMDType {
	 public final static int CONNECT=1;
	 public final static int BIND=2;
	 public final static int UDP=3;
	
	public static String valueOf(int cmdType){
		String e="";
		switch (cmdType) {
		case 1:
			e="CONNECT";
			break;
		case 2:
			//e="BIND";
			break;
		case 3:
			//e="UDP";
			break;
		default:
			break;
		}
		return e;
	}
	
}
