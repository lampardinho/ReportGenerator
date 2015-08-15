/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.ejbinwar.controller;

import org.jboss.as.quickstarts.ejbinwar.ejb.GreeterEJB;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * A simple managed bean that is used to invoke the GreeterEJB and store the response. The response is obtained by invoking
 * getMessage().
 * 
 * @author paul.robinson@redhat.com, 2011-12-21
 */
@Named("reporter")
@SessionScoped
public class Greeter implements Serializable {

    /**
     * Injected GreeterEJB client
     */
    @EJB
    private GreeterEJB greeterEJB;


    private List<String> tariffs;
    private String selectedTariff;

    @PostConstruct
    public void init() {
        tariffs = greeterEJB.getRestTariffs();
    }



    public void createPdf()
    {
        ByteArrayOutputStream baosPDF = null;

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse resp = (HttpServletResponse)context.getExternalContext().getResponse();

        try
        {
            baosPDF = greeterEJB.generatePDFDocumentBytes(selectedTariff);

            StringBuffer sbFilename = new StringBuffer();
            sbFilename.append("ecare-report-");
            sbFilename.append(System.currentTimeMillis());
            sbFilename.append(".pdf");

            resp.setHeader("Cache-Control", "max-age=30");

            resp.setContentType("application/pdf");

            StringBuffer sbContentDispValue = new StringBuffer();
            sbContentDispValue.append("inline");
            sbContentDispValue.append("; filename=");
            sbContentDispValue.append(sbFilename);

            resp.setHeader("Content-disposition", sbContentDispValue.toString());

            resp.setContentLength(baosPDF.size());

            ServletOutputStream sos;

            sos = resp.getOutputStream();

            baosPDF.writeTo(sos);

            sos.flush();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (baosPDF != null)
            {
                baosPDF.reset();
            }
        }

    }


    public List<String> getTariffs()
    {
        return tariffs;
    }

    public String getSelectedTariff()
    {
        return selectedTariff;
    }

    public void setSelectedTariff(String selectedTariff)
    {
        this.selectedTariff = selectedTariff;
    }

}
