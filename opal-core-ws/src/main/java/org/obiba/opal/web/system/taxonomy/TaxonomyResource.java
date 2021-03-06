/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.taxonomy;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/system/conf/taxonomy/{name}")
public class TaxonomyResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private ApplicationContext applicationContext;

  @PathParam("name")
  private String name;

  @GET
  public Opal.TaxonomyDto getTaxonomy() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(name);
    return Dtos.asDto(taxonomy);
  }

  @PUT
  public Response updateTaxonomy(Opal.TaxonomyDto dto) {
    taxonomyService.saveTaxonomy(new Taxonomy(name), Dtos.fromDto(dto));
    return Response.ok().build();
  }

  @DELETE
  public Response deleteTaxonomy() {
    taxonomyService.deleteTaxonomy(name);
    return Response.ok().build();
  }

  @Path("vocabularies")
  public VocabulariesResource getVocabularies() {
    VocabulariesResource resource = applicationContext.getBean(VocabulariesResource.class);
    resource.setTaxonomyName(name);
    return resource;
  }

  @Path("vocabulary/{vocabularyName}")
  public VocabularyResource getVocabulary(@PathParam("vocabularyName") String vocabularyName) {
    VocabularyResource resource = applicationContext.getBean(VocabularyResource.class);
    resource.setTaxonomyName(name);
    resource.setVocabularyName(vocabularyName);
    return resource;
  }
}
