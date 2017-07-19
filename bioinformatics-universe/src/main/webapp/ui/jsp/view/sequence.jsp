<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<div style="display: none">
	<span id="sequence-tab">${sequenceTab}</span>
</div>

<div class="jumbotron backgroundtheme">
    <div class="container">
			<div>Description</div>
	</div>
</div>

<div class="container-fluid">
	<div class="form-group col-md-12">
		<%@ include file="sequence-navbar.jsp"%>
		<div class="col-md-10">
			<div class="row">
				<div class="col-md-4 my-textarea">
					<div class="row">
						<textarea class="form-control" placeholder="test" required rows=8 id="query-field"> </textarea>
					</div>
					<div class="row">
						OR <br /> 
						<label class="btn btn-default"> 
						Choose File <input id="first-file" type="file" style="display: none" onchange="$('#first-file-info').html(this.files[0].name)">
						</label> 
						<span class='label label-info' id="first-file-info"></span>
					</div>
				</div>
				<div class="col-md-4 my-textarea">
					<div class="row">
						<textarea class="form-control" placeholder="test" required rows=8 id="query-field"> </textarea>
					</div>
					<div class="row">
						OR <br /> 
						<label class="btn btn-default"> 
						Choose File <input id="second-file" type="file" style="display: none" onchange="$('#second-file-info').html(this.files[0].name)">
						</label> <span class='label label-info' id="second-file-info"></span>
					</div>
					<div class="row">
						<button id="Go" class="btn btn-primary btn-md pull-right" type="button">
							<span>Go!</span>
						</button>
					</div>
				</div>
				<div class="col-md-2 panel panel-default">
					<div class="panel-body">
						<p class="text-primary">Set options:</p>
						<ul class="nav nav-pills nav-stacked">
							<li>
								<div class="form-group">
									<small>First file delimiter:</small> 
									<select id="first-delim" class="form-control input-sm">
										<option selected='selected' disabled>select</option>
										<option>tab</option>
										<option>comma</option>
										<option>semicolon</option>
										<option>vertical bar</option>
									</select> 
									<small>First file column:</small>
									<input id="first-col" type="text" class="form-control input-sm">
								</div>
							</li>
							<li>
								<div class="form-group">
									<small>Second file delimiter:</small> 
									<select id="second-delim" class="form-control input-sm">
										<option selected='selected' disabled>select</option>
										<option>tab</option>
										<option>comma</option>
										<option>semicolon</option>
										<option>vertical bar</option>
									</select>
									<small>Second file column:</small>
									<input id="second-col" type="text" class="form-control input-sm">
								</div>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
	<p><a id="results" href="#"></a></p>
</div>
