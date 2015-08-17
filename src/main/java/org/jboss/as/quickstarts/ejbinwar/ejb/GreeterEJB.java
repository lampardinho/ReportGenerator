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
package org.jboss.as.quickstarts.ejbinwar.ejb;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.as.quickstarts.ejbinwar.UserInfo;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ejb.Stateful;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * A simple Hello World EJB. The EJB does not use an interface.
 * 
 * @author paul.robinson@redhat.com, 2011-12-21
 */
@Stateful
public class GreeterEJB {


    public java.util.List<String> getRestTariffs()
    {

        Set<String> tariffs = new HashSet<>();


        try {
            ResteasyClient client = new ResteasyClientBuilder().build();

            ResteasyWebTarget target = client
                    .target("http://localhost:8085/fullstack/rest/tariffs");

            Response response = target.request().get();
            tariffs = response.readEntity(Set.class);
            System.out.println(tariffs.size());

            response.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
        return new LinkedList<>(tariffs);


    }



    private java.util.Set<String> getRestUsers(String selectedTariff)
    {
        Set<String> users = new HashSet<>();


        try {
            ResteasyClient client = new ResteasyClientBuilder().build();
            ObjectMapper mapper = new ObjectMapper();
            ResteasyWebTarget target = client
                    .target("http://localhost:8085/fullstack/rest/users?tariff="+selectedTariff);

            /*JsonNode response = target.request().get(JsonNode.class);
            users = mapper.readValue(
                    mapper.treeAsTokens(response),
                    new TypeReference<HashSet<UserInfo>>(){ }
            );*/
            Response response = target.request().get();
            users = response.readEntity(Set.class);
            System.out.println(users.size());

            response.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
        return users;


    }




    public ByteArrayOutputStream generatePDFDocumentBytes(String selectedTariff) throws DocumentException
    {
        java.util.Set<String> users = getRestUsers(selectedTariff);

        Document doc = new Document();

        ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
        PdfWriter docWriter = null;

        try
        {
            docWriter = PdfWriter.getInstance(doc, baosPDF);

            doc.addAuthor(this.getClass().getName());
            doc.addCreationDate();
            doc.addProducer();
            doc.addCreator(this.getClass().getName());
            doc.addTitle(selectedTariff + " clients");
            doc.addKeywords("pdf, itext, Java, ecare, http");

            doc.setPageSize(PageSize.LETTER);

            HeaderFooter footer = new HeaderFooter(new Phrase("E-Care report"), false);

            doc.setFooter(footer);

            doc.open();

            doc.add(new Paragraph(selectedTariff + " clients"));
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4); // 3 columns.

            PdfPCell cell1 = new PdfPCell(new Paragraph("Name"));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Surname"));
            PdfPCell cell3 = new PdfPCell(new Paragraph("Address"));
            PdfPCell cell4 = new PdfPCell(new Paragraph("Email"));

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);



            for (Iterator<String> it = users.iterator(); it.hasNext(); )
            {
                String user = it.next();

                table.addCell(new PdfPCell(new Paragraph(user.split(" ")[0])));
                table.addCell(new PdfPCell(new Paragraph(user.split(" ")[1])));
                table.addCell(new PdfPCell(new Paragraph(user.split(" ")[2])));
                table.addCell(new PdfPCell(new Paragraph(user.split(" ")[3])));
            }

            doc.add(table);





        }
        catch (DocumentException dex)
        {
            baosPDF.reset();
            throw dex;
        }
        finally
        {
            if (doc != null)
            {
                doc.close();
            }
            if (docWriter != null)
            {
                docWriter.close();
            }
        }

        if (baosPDF.size() < 1)
        {
            throw new DocumentException("document has " + baosPDF.size() + " bytes");
        }
        return baosPDF;


    }
}
