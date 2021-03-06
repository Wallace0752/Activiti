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
import org.activiti.bpmn.model.CustomProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class ServiceTaskXMLConverter extends BaseBpmnXMLConverter {
  
  public static String getXMLType() {
    return ELEMENT_TASK_SERVICE;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return ServiceTask.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_TASK_SERVICE;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
		ServiceTask serviceTask = new ServiceTask();
		BpmnXMLUtil.addXMLLocation(serviceTask, xtr);
		if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_CLASS))) {
			serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
			serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_CLASS));
			
		} else if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXPRESSION))) {
			serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
			serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXPRESSION));
			
		} else if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION))) {
			serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
			serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION));
		
		} else if ("##WebService".equals(xtr.getAttributeValue(null, ATTRIBUTE_TASK_IMPLEMENTATION))) {
		  serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE);
		  serviceTask.setOperationRef(parseOperationRef(xtr.getAttributeValue(null, ATTRIBUTE_TASK_OPERATION_REF), model));
		}
	
		serviceTask.setResultVariableName(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE));
		if (StringUtils.isEmpty(serviceTask.getResultVariableName())) {
		  serviceTask.setResultVariableName(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "resultVariable"));
		}
		
		if (StringUtils.isNotEmpty(serviceTask.getResultVariableName()) && (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType()) || 
		    ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType()))) {
		  
		  model.addProblem("'resultVariableName' not supported for service tasks using 'class' or 'delegateExpression", xtr);
		}
		
		serviceTask.setType(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TYPE));
		serviceTask.setExtensionId(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXTENSIONID));
	
		parseChildElements(getXMLElementName(), serviceTask, xtr);
		
		return serviceTask;
  }
  
  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    
    ServiceTask serviceTask = (ServiceTask) element;
    
    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_CLASS, serviceTask.getImplementation(), xtw);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_EXPRESSION, serviceTask.getImplementation(), xtw);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION, serviceTask.getImplementation(), xtw);
    }
    
    if (StringUtils.isNotEmpty(serviceTask.getResultVariableName())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE, serviceTask.getResultVariableName(), xtw);
    }
    if (StringUtils.isNotEmpty(serviceTask.getType())) {
      writeQualifiedAttribute(ATTRIBUTE_TYPE, serviceTask.getType(), xtw);
    }
    if (StringUtils.isNotEmpty(serviceTask.getExtensionId())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_EXTENSIONID, serviceTask.getExtensionId(), xtw);
    }
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    ServiceTask serviceTask = (ServiceTask) element;
    
    if (serviceTask.getCustomProperties().size() > 0) {
      for (CustomProperty customProperty : serviceTask.getCustomProperties()) {
        
        if (StringUtils.isEmpty(customProperty.getSimpleValue())) {
          continue;
        }
        
        if (didWriteExtensionStartElement == false) {
          xtw.writeStartElement(ELEMENT_EXTENSIONS);
          didWriteExtensionStartElement = true;
        }
        xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD, ACTIVITI_EXTENSIONS_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_FIELD_NAME, customProperty.getName());
        if ((customProperty.getSimpleValue().contains("${") || customProperty.getSimpleValue().contains("#{")) &&
            customProperty.getSimpleValue().contains("}")) {
          
          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ATTRIBUTE_FIELD_EXPRESSION, ACTIVITI_EXTENSIONS_NAMESPACE);
        } else {
          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD_STRING, ACTIVITI_EXTENSIONS_NAMESPACE);
        }
        xtw.writeCharacters(customProperty.getSimpleValue());
        xtw.writeEndElement();
        xtw.writeEndElement();
      }
    } else {
      didWriteExtensionStartElement = FieldExtensionUtil.writeFieldExtensions(serviceTask.getFieldExtensions(), didWriteExtensionStartElement, xtw);
    }
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
