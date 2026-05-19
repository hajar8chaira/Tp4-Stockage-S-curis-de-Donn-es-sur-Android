package com.example.securestorageapp.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.securestorageapp.R;
import com.example.securestorageapp.security.SecurityAnalyzer;
import java.util.List;

public class SecurityRisksFragment extends Fragment {

    private LinearLayout llRisksContainer;
    private SecurityAnalyzer securityAnalyzer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security_risks, container, false);

        securityAnalyzer = new SecurityAnalyzer(requireContext());
        llRisksContainer = view.findViewById(R.id.llRisksContainer);
        Button btnAnalyze = view.findViewById(R.id.btnAnalyze);

        btnAnalyze.setOnClickListener(v -> runDiagnostic());
        runDiagnostic();

        return view;
    }

    private void runDiagnostic() {
        llRisksContainer.removeAllViews();
        List<SecurityAnalyzer.SecurityRisk> risks = securityAnalyzer.analyzeSecurityRisks();

        if (risks.isEmpty()) {
            View congratsView = LayoutInflater.from(requireContext()).inflate(R.layout.item_security_risk, llRisksContainer, false);
            TextView tvTitle = congratsView.findViewById(R.id.tvRiskTitle);
            TextView tvDesc = congratsView.findViewById(R.id.tvRiskDescription);
            TextView tvBadge = congratsView.findViewById(R.id.tvSeverityBadge);
            TextView tvHint = congratsView.findViewById(R.id.tvActionHint);

            tvTitle.setText("Félicitations !");
            tvTitle.setTextColor(Color.parseColor("#E91E63"));
            tvDesc.setText("Aucune faille de stockage ou d'exécution détectée.");
            tvBadge.setText("SÉCURISÉ");
            tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

            if (!SecurityAnalyzer.mitigatedRisks.isEmpty()) {
                tvHint.setText("Cliquer pour réinitialiser la simulation");
                congratsView.setOnClickListener(v -> {
                    SecurityAnalyzer.mitigatedRisks.clear();
                    Toast.makeText(requireContext(), "Simulations réinitialisées.", Toast.LENGTH_SHORT).show();
                    runDiagnostic();
                });
            } else {
                tvHint.setVisibility(View.GONE);
            }

            llRisksContainer.addView(congratsView);
        } else {
            for (SecurityAnalyzer.SecurityRisk risk : risks) {
                View riskCard = LayoutInflater.from(requireContext()).inflate(R.layout.item_security_risk, llRisksContainer, false);
                TextView tvTitle = riskCard.findViewById(R.id.tvRiskTitle);
                TextView tvDesc = riskCard.findViewById(R.id.tvRiskDescription);
                TextView tvBadge = riskCard.findViewById(R.id.tvSeverityBadge);

                tvTitle.setText(risk.getTitle());
                tvDesc.setText(risk.getDescription());
                tvBadge.setText(risk.getSeverity().name());

                int badgeColor;
                switch (risk.getSeverity()) {
                    case HIGH:
                        badgeColor = Color.parseColor("#D32F2F");
                        break;
                    case MEDIUM:
                        badgeColor = Color.parseColor("#FF9800");
                        break;
                    case LOW:
                    default:
                        badgeColor = Color.parseColor("#7F8C8D");
                        break;
                }
                tvBadge.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

                riskCard.setOnClickListener(v -> showRiskDetailDialog(risk));
                llRisksContainer.addView(riskCard);
            }
        }
    }

    private void showRiskDetailDialog(SecurityAnalyzer.SecurityRisk risk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_security_detail, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvSeverity = dialogView.findViewById(R.id.tvDialogSeverity);
        TextView tvDesc = dialogView.findViewById(R.id.tvDialogDescription);
        TextView tvRec = dialogView.findViewById(R.id.tvDialogRecommendation);
        TextView tvCode = dialogView.findViewById(R.id.tvDialogCodeSnippet);
        Button btnFix = dialogView.findViewById(R.id.btnSimulateFix);
        Button btnClose = dialogView.findViewById(R.id.btnDialogClose);

        tvTitle.setText(risk.getTitle());
        tvSeverity.setText(risk.getSeverity().name());
        tvDesc.setText(risk.getDescription());
        tvRec.setText(risk.getRecommendation());
        tvCode.setText(risk.getCodeSnippet());

        int badgeColor;
        switch (risk.getSeverity()) {
            case HIGH:
                badgeColor = Color.parseColor("#D32F2F");
                break;
            case MEDIUM:
                badgeColor = Color.parseColor("#FF9800");
                break;
            case LOW:
            default:
                badgeColor = Color.parseColor("#7F8C8D");
                break;
        }
        tvSeverity.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

        AlertDialog dialog = builder.create();

        btnFix.setOnClickListener(v -> {
            SecurityAnalyzer.mitigatedRisks.add(risk.getTitle());
            dialog.dismiss();
            Toast.makeText(requireContext(), "Correction simulée avec succès !", Toast.LENGTH_SHORT).show();
            runDiagnostic();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
