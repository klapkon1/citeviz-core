package cz.uhk.fim.citeviz.graph.categorizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import cz.uhk.fim.citeviz.graph.engine.Renderer;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.views.GraphBasedView;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.AuthorCategorization;
import cz.uhk.fim.citeviz.model.AuthorFullDetail;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.PaperCategorization;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class CategorizerBuilder {
	
	public static Categorizer<IdRecord, ?> buildCategorizer(Renderer renderer, JComponent pnlStatLegend, JComboBox<?> cboCategorization, DataInterface dataInterface){
		Graph graph = null;
		if (renderer.getView() != null && renderer.getView() instanceof GraphBasedView) {
			graph = ((GraphBasedView) renderer.getView()).getGraph();
		}
		
		if (cboCategorization.getSelectedItem() instanceof AuthorCategorization){
			return new Categorizer<IdRecord, Object>(renderer.getDisplayRecords(), pnlStatLegend, graph) {
				
				@Override
				public Object extractValueFromOjbject(IdRecord object) {
					if (object instanceof Author){
						AuthorCategorization selectedCategory = (AuthorCategorization) cboCategorization.getSelectedItem();
						Author author = (Author) object;
						switch (selectedCategory) {
							case CHILDS : return author.getChildsCount(); 
							case PARENTS : return author.getParentsCount();
							case PAPERS_COUNT : return author.getPapersCount();
							case CITATION_INDEX : return author.getCitationIndex();
							default: 
								if (author instanceof AuthorFullDetail)
									switch (selectedCategory) {
										case PROVIDER_CATEGORY_0 : return ((AuthorFullDetail)author).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_0)); 
										case PROVIDER_CATEGORY_1 : return ((AuthorFullDetail)author).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_1)); 
										case PROVIDER_CATEGORY_2 : return ((AuthorFullDetail)author).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_2)); 
										default : return null;
									}
						}
					}
					return null;
				}
			};	
		}
		
		if (cboCategorization.getSelectedItem() instanceof PaperCategorization){
			return new Categorizer<IdRecord, Object>(renderer.getDisplayRecords(), pnlStatLegend, graph) {
				
				@Override
				public Object extractValueFromOjbject(IdRecord object) {
					if (object instanceof Paper){
						PaperCategorization selectedCategory = (PaperCategorization) cboCategorization.getSelectedItem();
						Paper paper = (Paper) object;
						switch (selectedCategory) {
							case CHILDS : return paper.getChildsCount(); 
							case PARENTS : return paper.getParentsCount();
							case YEAR : return paper.getYear();
							case CITATION_INDEX : return paper.getCitationIndex();
							default: 
								if (paper instanceof PaperFullDetail)
									switch (selectedCategory) {
										case PROVIDER_CATEGORY_0 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_0)); 
										case PROVIDER_CATEGORY_1 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_1)); 
										case PROVIDER_CATEGORY_2 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_2)); 
										case PROVIDER_CATEGORY_3 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_3));  
										case PROVIDER_CATEGORY_4 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_4)); 
										case PROVIDER_CATEGORY_5 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_5));
										case PROVIDER_CATEGORY_6 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_6));
										case PROVIDER_CATEGORY_7 : return ((PaperFullDetail)paper).getOtherDataMultiValue(dataInterface.getPaperCategorizationMapping().get(PaperCategorization.PROVIDER_CATEGORY_7));
										default : return null;
									}
						}
					}
					return null;
				}
			};
		}
		
		throw new IllegalArgumentException("Unsupported categorization type " + cboCategorization.getSelectedItem().getClass());
	}

	public static PaperCategorization[] paperCategories(DataInterface dataInterface) {
		List<PaperCategorization> values = new ArrayList<>();
		values.add(PaperCategorization.NONE);
		values.add(PaperCategorization.YEAR);
		values.add(PaperCategorization.CHILDS);
		values.add(PaperCategorization.PARENTS);
		values.add(PaperCategorization.CITATION_INDEX);
		
		if (dataInterface.getPaperCategorizationMapping() != null){
			for (Entry<PaperCategorization, String>  item : dataInterface.getPaperCategorizationMapping().entrySet()) {
				values.add(item.getKey());
			}
		}
		
		return values.toArray(new PaperCategorization[]{});
	}
	
	public static AuthorCategorization[] authorCategories(DataInterface dataInterface) {
		List<AuthorCategorization> values = new ArrayList<>();
		values.add(AuthorCategorization.NONE);
		values.add(AuthorCategorization.PAPERS_COUNT);
		values.add(AuthorCategorization.CHILDS);
		values.add(AuthorCategorization.PARENTS);
		values.add(AuthorCategorization.CITATION_INDEX);
		
		if (dataInterface.getAuthorCategorizationMapping() != null){
			for (Entry<AuthorCategorization, String>  item : dataInterface.getAuthorCategorizationMapping().entrySet()) {
				values.add(item.getKey());
			}
		}
		
		return values.toArray(new AuthorCategorization[]{});
	}
}