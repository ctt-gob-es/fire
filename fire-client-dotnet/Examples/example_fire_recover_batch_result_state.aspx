<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_fire_recover_batch_result_state.aspx.cs" Inherits="example_fire_recover_batch_result_state" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Recuperar estado de firma del lote</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><b>Progreso de la firma (0-1)):</b><br /> <asp:Label runat="server" id="ProgressBatch"></asp:Label>
    </div>
    </form>
</body>
</html>
