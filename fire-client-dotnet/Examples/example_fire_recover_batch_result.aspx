<%@ Page Language="C#" AutoEventWireup="true" 
    CodeFile="example_fire_recover_batch_result.aspx.cs" 
    Inherits="example_fire_recover_batch_result" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Creacion de batch</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
    
        <br /><b>Proveedor:</b><br /> <asp:Label runat="server" id="ProviderName"></asp:Label>
        <br /><b>Resultado de firma:</b><br /> <asp:Label runat="server" id="Result1"></asp:Label>
        <br /><b>Resultado de firma:</b><br /> <asp:Label runat="server" id="Result2"></asp:Label>

    </div>
    </form>
</body>
</html>
