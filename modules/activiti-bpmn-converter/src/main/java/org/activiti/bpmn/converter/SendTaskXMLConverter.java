/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.converter.util.FieldExtensionUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SendTask;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class SendTaskXMLConverter extends BaseBpmnXMLConverter {
  
  public static String getXMLType() {
    return ELEMENT_TASK_SEND;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return SendTask.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_TASK_SEND;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
		SendTask sendTask = new SendTask();
		BpmnXMLUtil.addXMLLocation(sendTask, xtr);
		sendTask.setType(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TYPE));
		
		if ("##WebService".equals(xtr.getAttributeValue(null, ATTRIBUTE_TASK_IMPLEMENTATION))) {
		  sendTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE);
		  sendTask.setOperationRef(parseOperationRef(xtr.getAttributeValue(null, ATTRIBUTE_TASK_OPERATION_REF), model));
    }
		
		parseChildElements(getXMLElementName(), sendTask, xtr);
		
		return sendTask;
  }
  
  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    
    SendTask sendTask = (SendTask) element;
    
    if (StringUtils.isNotEmpty(sendTask.getType())) {
      writeQualifiedAttribute(ATTRIBUTE_TYPE, sendTask.getType(), xtw);
    }
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    SendTask sendTask = (SendTask) element;
    didWriteExtensionStartElement = FieldExtensionUtil.writeFieldExtensions(sendTask.getFieldExtensions(), didWriteExtensionStartElement, xtw);
  }
  
  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }
  
  protected String parseOperationRef(String operationRef, BpmnModel model) {
    String result = null;
    if (StringUtils.isNotEmpty(operationRef)) {
      int indexOfP = operationRef.indexOf(':');
      if (indexOfP != -1) {
        String prefix = operationRef.substring(0, indexOfP);
        String resolvedNamespace = model.getNamespace(prefix);
        result = resolvedNamespace + ":" + operationRef.substring(indexOfP + 1);
      } else {
        result = model.getTargetNamespace() + ":" + operationRef;
      }
    }
    return result;
  }
}
